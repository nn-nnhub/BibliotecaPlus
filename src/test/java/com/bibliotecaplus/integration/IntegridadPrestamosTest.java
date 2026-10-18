package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:rfc012;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.h2.console.enabled=false",
        "spring.jpa.open-in-view=false"})
class IntegridadPrestamosTest {
    @Autowired private DataSource dataSource;
    @Autowired private PrestamoService servicio;
    @Autowired private LibroRepository libros;
    @Autowired private UsuarioRepository usuarios;
    @SpyBean private PrestamoRepository prestamos;
    private Libro libro;
    private Usuario usuario;

    // Sin @Transactional: cada lectura JDBC abre su propia conexión, fuera de JPA.
    @BeforeEach
    void prepararDatosAislados() throws Exception {
        try (Connection conexion = dataSource.getConnection()) {
            String url = conexion.getMetaData().getURL();
            assertTrue(url.startsWith("jdbc:h2:mem:rfc012"), "Base inesperada: " + url);
            assertFalse(url.contains("bibliotecadb"));
            assertTrue(conexion.getAutoCommit());
            System.out.println("RFC012 BASE VERIFICADA: " + url);
        }
        libro = libros.saveAndFlush(new Libro("RFC012", "Prueba aislada", UUID.randomUUID().toString()));
        usuario = usuarios.findAll().get(0); // Datos de ejemplo creados en la H2 en memoria.
    }

    private boolean disponiblePersistido() throws Exception {
        try (Connection conexion = dataSource.getConnection();
             var consulta = conexion.prepareStatement("select disponible from libro where id = ?")) {
            consulta.setLong(1, libro.getId());
            try (var resultado = consulta.executeQuery()) {
                assertTrue(resultado.next());
                return resultado.getBoolean(1);
            }
        }
    }

    private Estado estadoPersistido() throws Exception {
        try (Connection conexion = dataSource.getConnection();
             var consulta = conexion.prepareStatement(
                     "select devuelto, fecha_devolucion, multa from prestamo where libro_id = ?")) {
            consulta.setLong(1, libro.getId());
            try (var resultado = consulta.executeQuery()) {
                int cantidad = 0;
                boolean devuelto = false;
                LocalDate fecha = null;
                double multa = 0;
                while (resultado.next()) {
                    cantidad++;
                    devuelto = resultado.getBoolean(1);
                    fecha = resultado.getObject(2, LocalDate.class);
                    multa = resultado.getDouble(3);
                }
                return new Estado(cantidad, devuelto, fecha, multa);
            }
        }
    }

    private AtomicBoolean inyectarFallo(boolean disponibilidadTrasGuardarLibro) {
        AtomicBoolean alcanzado = new AtomicBoolean();
        doAnswer(invocacion -> {
            // En ANTES esta conexión independiente debe ver el cambio ya confirmado.
            // Se imprime sin exigirlo: DESPUÉS una transacción puede ocultarlo hasta commit.
            boolean visible = disponiblePersistido();
            System.out.printf("RFC012 PUNTO DE FALLO: libro.disponible=%s; cambio del libro visible=%s%n",
                    visible, visible == disponibilidadTrasGuardarLibro);
            alcanzado.set(true);
            throw new FalloControlado();
        }).when(prestamos).save(any(Prestamo.class));
        return alcanzado;
    }

    @Test
    void registroFallidoDebeConservarLibroDisponible() throws Exception {
        assertTrue(disponiblePersistido());
        assertEquals(0, estadoPersistido().cantidad());
        AtomicBoolean alcanzado = inyectarFallo(false);
        assertThrows(FalloControlado.class, () -> servicio.registrar(
                libro.getId(), usuario.getId(), "DOMICILIO", LocalDate.now().plusDays(7)));
        assertTrue(alcanzado.get());
        verify(prestamos, times(1)).save(any(Prestamo.class));
        boolean disponible = disponiblePersistido();
        Estado estado = estadoPersistido();
        System.out.printf("RFC012 REGISTRO TRAS FALLO: disponible=%s; prestamos=%d%n", disponible, estado.cantidad());
        assertAll("Atomicidad del registro",
                () -> assertTrue(disponible, "El libro debe seguir disponible si no se guardó el préstamo"),
                () -> assertEquals(0, estado.cantidad()));
    }

    @Test
    void devolucionFallidaDebeConservarLibroOcupadoYPrestamoActivo() throws Exception {
        libro.setDisponible(false);
        libros.saveAndFlush(libro);
        Prestamo prestamo = new Prestamo(libro.getId(), usuario.getId(),
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        prestamo.setTipoPrestamo("ESPECIAL");
        prestamo = prestamos.saveAndFlush(prestamo);
        Long id = prestamo.getId();
        assertFalse(disponiblePersistido());
        Estado inicial = estadoPersistido();
        assertEquals(new Estado(1, false, null, 0), inicial);
        AtomicBoolean alcanzado = inyectarFallo(true);
        assertThrows(FalloControlado.class, () -> servicio.devolver(id));
        assertTrue(alcanzado.get());
        verify(prestamos, times(1)).save(any(Prestamo.class));
        boolean disponible = disponiblePersistido();
        Estado estado = estadoPersistido();
        System.out.printf("RFC012 DEVOLUCION TRAS FALLO: disponible=%s; estado=%s%n", disponible, estado);
        assertAll("Atomicidad de la devolución",
                () -> assertFalse(disponible, "El libro debe seguir ocupado si no se guardó la devolución"),
                () -> assertEquals(inicial, estado));
    }

    @Test
    void registroYDevolucionNormalesPersistenAmbosEstados() throws Exception {
        Prestamo prestamo = servicio.registrar(libro.getId(), usuario.getId(),
                "ESPECIAL", LocalDate.now().plusDays(7));
        assertFalse(disponiblePersistido());
        assertEquals(new Estado(1, false, null, 0), estadoPersistido());
        servicio.devolver(prestamo.getId());
        assertTrue(disponiblePersistido());
        assertEquals(new Estado(1, true, LocalDate.now(), 0), estadoPersistido());
        System.out.println("RFC012 FLUJO NORMAL: registro y devolucion consistentes; multa=0.0");
    }

    private record Estado(int cantidad, boolean devuelto, LocalDate fechaDevolucion, double multa) {}
    private static class FalloControlado extends RuntimeException {
        FalloControlado() { super("RFC012: fallo controlado antes de persistir el prestamo"); }
    }
}
