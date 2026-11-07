package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.validation.ValidadorPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:rfc013-despues;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.h2.console.enabled=false",
        "spring.jpa.open-in-view=false"})
@AutoConfigureMockMvc
class ConcurrenciaPrestamosDespuesTest {
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mvc;
    @Autowired private LibroRepository libros;
    @Autowired private UsuarioRepository usuarios;
    @SpyBean private ValidadorPrestamo validador;
    @SpyBean private PrestamoRepository prestamos;
    private Long usuarioId;

    @BeforeEach
    void verificarBaseYTimeout() throws Exception {
        try (var conexion = dataSource.getConnection(); var consulta = conexion.createStatement()) {
            assertEquals("jdbc:h2:mem:rfc013-despues", conexion.getMetaData().getURL());
            try (var resultado = consulta.executeQuery("call lock_timeout()")) {
                assertTrue(resultado.next());
                assertEquals(2000, resultado.getInt(1));
            }
        }
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        usuarioId = usuarios.findAll().get(0).getId();
        System.out.println("RFC013 DESPUES BASE: jdbc:h2:mem:rfc013-despues; LOCK_TIMEOUT=2000 ms");
    }

    private Libro nuevoLibro() {
        return libros.saveAndFlush(new Libro("RFC013 DESPUES", "Prueba", UUID.randomUUID().toString()));
    }

    private MockHttpServletResponse registrar(Long libroId) throws Exception {
        return mvc.perform(post("/api/prestamos").param("libroId", libroId.toString())
                        .param("usuarioId", usuarioId.toString()).param("tipoPrestamo", "DOMICILIO")
                        .param("fechaVencimiento", LocalDate.now().plusDays(7).toString()))
                .andReturn().getResponse();
    }

    private CountDownLatch retenerPrimeraSolicitud(CountDownLatch liberar) {
        CountDownLatch adquirido = new CountDownLatch(1);
        AtomicBoolean primera = new AtomicBoolean(true);
        doAnswer(invocacion -> {
            invocacion.callRealMethod();
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            if (primera.getAndSet(false)) {
                adquirido.countDown();
                assertTrue(liberar.await(10, TimeUnit.SECONDS), "El coordinador debe liberar la primera solicitud");
            }
            return null;
        }).when(validador).validar(any(), any());
        return adquirido;
    }

    private void esperarContencionReal() {
        await().pollInterval(Duration.ofMillis(20)).atMost(Duration.ofSeconds(1)).until(() ->
                jdbc.queryForObject("select count(*) from information_schema.sessions where blocker_id is not null",
                        Integer.class) > 0);
        System.out.println("RFC013 DESPUES CONTENCION: segunda sesion bloqueada observada en H2");
    }

    private void verificarEstado(Long libroId, int esperados, boolean disponible) {
        assertEquals(esperados, jdbc.queryForObject(
                "select count(*) from prestamo where libro_id = ? and devuelto = false", Integer.class, libroId));
        assertEquals(esperados, jdbc.queryForObject(
                "select count(*) from prestamo where libro_id = ?", Integer.class, libroId));
        assertEquals(disponible, jdbc.queryForObject("select disponible from libro where id = ?", Boolean.class, libroId));
    }

    @RepeatedTest(3)
    void mismoLibroProduce201Y400SinDuplicados() throws Exception {
        Libro libro = nuevoLibro();
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch adquirido = retenerPrimeraSolicitud(liberar);
        var ejecutor = Executors.newFixedThreadPool(2);
        try {
            var primera = ejecutor.submit(() -> registrar(libro.getId()));
            assertTrue(adquirido.await(5, TimeUnit.SECONDS));
            var segunda = ejecutor.submit(() -> registrar(libro.getId()));
            esperarContencionReal();
            liberar.countDown();
            assertEquals(201, primera.get(10, TimeUnit.SECONDS).getStatus());
            var rechazo = segunda.get(10, TimeUnit.SECONDS);
            assertEquals(400, rechazo.getStatus());
            assertTrue(rechazo.getContentAsString(StandardCharsets.UTF_8).contains("El libro no está disponible"));
            verificarEstado(libro.getId(), 1, false);
            System.out.println("RFC013 DESPUES MISMO LIBRO: HTTP=201/400; activos=1; disponible=false");
        } finally {
            liberar.countDown();
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void librosDiferentesNoEsperanElBloqueoAjeno() throws Exception {
        Libro primero = nuevoLibro();
        Libro segundo = nuevoLibro();
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch adquirido = retenerPrimeraSolicitud(liberar);
        var ejecutor = Executors.newFixedThreadPool(2);
        try {
            var primera = ejecutor.submit(() -> registrar(primero.getId()));
            assertTrue(adquirido.await(5, TimeUnit.SECONDS));
            var segunda = ejecutor.submit(() -> registrar(segundo.getId()));
            assertEquals(201, segunda.get(5, TimeUnit.SECONDS).getStatus());
            assertFalse(primera.isDone(), "El primer préstamo sigue retenido mientras el otro termina");
            verificarEstado(segundo.getId(), 1, false);
            liberar.countDown();
            assertEquals(201, primera.get(5, TimeUnit.SECONDS).getStatus());
            verificarEstado(primero.getId(), 1, false);
            System.out.println("RFC013 DESPUES DIFERENTES: HTTP=201/201; un activo por libro; sin bloqueo global");
        } finally {
            liberar.countDown();
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void timeoutDelBloqueoResponde409SinEscriturasParciales() throws Exception {
        Libro libro = nuevoLibro();
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch adquirido = retenerPrimeraSolicitud(liberar);
        var ejecutor = Executors.newFixedThreadPool(2);
        try {
            var primera = ejecutor.submit(() -> registrar(libro.getId()));
            assertTrue(adquirido.await(5, TimeUnit.SECONDS));
            var segunda = ejecutor.submit(() -> registrar(libro.getId()));
            esperarContencionReal();
            var rechazo = segunda.get(8, TimeUnit.SECONDS);
            assertEquals(409, rechazo.getStatus());
            assertTrue(rechazo.getContentAsString().contains("otra solicitud"));
            verificarEstado(libro.getId(), 0, true);
            liberar.countDown();
            assertEquals(201, primera.get(5, TimeUnit.SECONDS).getStatus());
            verificarEstado(libro.getId(), 1, false);
            System.out.println("RFC013 DESPUES TIMEOUT: HTTP=409; sin estado parcial; primera HTTP=201; activos=1");
        } finally {
            liberar.countDown();
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void falloDePersistenciaLiberaBloqueoYPermiteLaSolicitudEnEspera() throws Exception {
        Libro libro = nuevoLibro();
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch adquirido = retenerPrimeraSolicitud(liberar);
        AtomicBoolean fallarPrimera = new AtomicBoolean(true);
        doAnswer(invocacion -> {
            if (fallarPrimera.getAndSet(false)) {
                throw new IllegalArgumentException("RFC013 fallo controlado al guardar préstamo");
            }
            return prestamos.saveAndFlush(invocacion.getArgument(0));
        }).when(prestamos).save(any(Prestamo.class));
        var ejecutor = Executors.newFixedThreadPool(2);
        try {
            var primera = ejecutor.submit(() -> registrar(libro.getId()));
            assertTrue(adquirido.await(5, TimeUnit.SECONDS));
            var segunda = ejecutor.submit(() -> registrar(libro.getId()));
            esperarContencionReal();
            liberar.countDown();
            var fallo = primera.get(5, TimeUnit.SECONDS);
            assertEquals(400, fallo.getStatus());
            assertTrue(fallo.getContentAsString().contains("fallo controlado"));
            assertEquals(201, segunda.get(5, TimeUnit.SECONDS).getStatus());
            verificarEstado(libro.getId(), 1, false);
            System.out.println("RFC013 DESPUES ROLLBACK: primera HTTP=400 controlado; segunda HTTP=201; activos=1");
        } finally {
            liberar.countDown();
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
