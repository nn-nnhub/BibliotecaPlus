package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.service.PrestamoService;
import com.bibliotecaplus.validation.ValidadorPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:rfc013;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.open-in-view=false",
        "spring.h2.console.enabled=false"})
class ConcurrenciaPrestamosTest {
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private LibroRepository libros;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PrestamoService servicio;
    @SpyBean private ValidadorPrestamo validador;

    @BeforeEach
    void verificarAislamiento() throws Exception {
        try (var conexion = dataSource.getConnection()) {
            String url = conexion.getMetaData().getURL();
            assertEquals("jdbc:h2:mem:rfc013", url);
            assertTrue(conexion.getAutoCommit());
            System.out.println("RFC013 BASE VERIFICADA: " + url);
        }
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    private Libro crearLibro() {
        return libros.saveAndFlush(new Libro("RFC013", "Concurrencia", UUID.randomUUID().toString()));
    }

    private List<Resultado> registrarSimultaneamente(Long primerLibro, Long segundoLibro) throws Exception {
        var usuariosPrueba = usuarios.findAll();
        assertTrue(usuariosPrueba.size() >= 2);
        CyclicBarrier lecturasCompletadas = new CyclicBarrier(2);
        // El servicio ya leyó disponible=true en cada transacción. No se simulan datos ni escrituras.
        doAnswer(invocacion -> {
            invocacion.callRealMethod();
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            Libro libro = invocacion.getArgument(0);
            assertTrue(libro.isDisponible());
            System.out.printf("RFC013 LECTURA: hilo=%s; libro=%d; disponible=true; transaccion=true%n",
                    Thread.currentThread().getName(), libro.getId());
            lecturasCompletadas.await(10, TimeUnit.SECONDS);
            return null;
        }).when(validador).validar(any(), any());

        var ejecutor = Executors.newFixedThreadPool(2);
        try {
            var primero = ejecutor.submit(() -> registrar(primerLibro, usuariosPrueba.get(0).getId()));
            var segundo = ejecutor.submit(() -> registrar(segundoLibro, usuariosPrueba.get(1).getId()));
            return List.of(primero.get(30, TimeUnit.SECONDS), segundo.get(30, TimeUnit.SECONDS));
        } finally {
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(10, TimeUnit.SECONDS), "Los hilos deben terminar");
        }
    }

    private Resultado registrar(Long libroId, Long usuarioId) {
        try {
            var prestamo = servicio.registrar(libroId, usuarioId, "DOMICILIO", LocalDate.now().plusDays(7));
            return new Resultado(true, prestamo.getId(), null);
        } catch (IllegalArgumentException rechazo) {
            // Solo el rechazo de negocio se trata como resultado controlado. Errores de
            // barrera, SQL, bloqueo o infraestructura hacen fallar la prueba como errores.
            return new Resultado(false, null, rechazo.getMessage());
        }
    }

    private int activos(Long libroId) {
        return jdbc.queryForObject("select count(*) from prestamo where libro_id = ? and devuelto = false",
                Integer.class, libroId);
    }

    private boolean disponible(Long libroId) {
        return jdbc.queryForObject("select disponible from libro where id = ?", Boolean.class, libroId);
    }

    @RepeatedTest(3)
    void mismoLibroDebePermitirSoloUnPrestamoActivo() throws Exception {
        Libro libro = crearLibro();
        var resultados = registrarSimultaneamente(libro.getId(), libro.getId());
        long exitos = resultados.stream().filter(Resultado::exito).count();
        int cantidad = activos(libro.getId());
        boolean disponibilidad = disponible(libro.getId());
        System.out.printf("RFC013 MISMO LIBRO: exitos=%d; activos=%d; disponible=%s; resultados=%s%n",
                exitos, cantidad, disponibilidad, resultados);
        assertAll("Un único préstamo activo por libro",
                () -> assertEquals(1, exitos, "Debe existir un éxito y un rechazo controlado"),
                () -> assertEquals(1, cantidad, "No deben persistir préstamos activos duplicados"),
                () -> assertFalse(disponibilidad));
    }

    @Test
    void librosDiferentesDebenPermitirAmbosPrestamos() throws Exception {
        Libro primero = crearLibro();
        Libro segundo = crearLibro();
        var resultados = registrarSimultaneamente(primero.getId(), segundo.getId());
        long exitos = resultados.stream().filter(Resultado::exito).count();
        int activosPrimero = activos(primero.getId());
        int activosSegundo = activos(segundo.getId());
        System.out.printf("RFC013 LIBROS DIFERENTES: exitos=%d; activosPrimero=%d; activosSegundo=%d%n",
                exitos, activosPrimero, activosSegundo);
        assertAll("Préstamos de libros diferentes",
                () -> assertEquals(2, exitos),
                () -> assertEquals(1, activosPrimero),
                () -> assertEquals(1, activosSegundo),
                () -> assertFalse(disponible(primero.getId())),
                () -> assertFalse(disponible(segundo.getId())));
    }

    private record Resultado(boolean exito, Long prestamoId, String rechazo) {}
}
