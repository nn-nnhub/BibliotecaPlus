package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:rfc014-antes;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.h2.console.enabled=false",
        "spring.jpa.open-in-view=false"})
@AutoConfigureMockMvc
class FiltrosHistorialHttpTest {
    @Autowired private DataSource dataSource;
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private PrestamoRepository prestamos;
    @Autowired private LibroRepository libros;
    @Autowired private UsuarioRepository usuarios;
    private final List<Long> idsPrestamos = new ArrayList<>();
    private Long usuarioUno;
    private Long libroUno;

    @BeforeEach
    void prepararHistorialAislado() throws Exception {
        // Verificar la conexión antes de limpiar exclusivamente esta H2 en memoria.
        try (var conexion = dataSource.getConnection()) {
            assertEquals("jdbc:h2:mem:rfc014-antes", conexion.getMetaData().getURL());
            assertTrue(conexion.getAutoCommit());
        }
        System.out.println("RFC014 BASE VERIFICADA: jdbc:h2:mem:rfc014-antes");
        prestamos.deleteAll();
        libros.deleteAll();
        usuarios.deleteAll();
        idsPrestamos.clear();
        var u1 = usuarios.saveAndFlush(new Usuario("Ana", "RFC014", "81000001", "ana@rfc014.test", "111"));
        var u2 = usuarios.saveAndFlush(new Usuario("Luis", "RFC014", "81000002", "luis@rfc014.test", "222"));
        var l1 = new Libro("Libro A RFC014", "Autor A", "RFC014-A");
        var l2 = new Libro("Libro B RFC014", "Autor B", "RFC014-B");
        l1.setDisponible(false);
        l2.setDisponible(false);
        l1 = libros.saveAndFlush(l1);
        l2 = libros.saveAndFlush(l2);
        usuarioUno = u1.getId();
        libroUno = l1.getId();
        guardar(l1.getId(), u1.getId(), false); // P1: U1/L1 activo
        guardar(l1.getId(), u1.getId(), true);  // P2: U1/L1 devuelto
        guardar(l2.getId(), u1.getId(), true);  // P3: U1/L2 devuelto
        guardar(l1.getId(), u2.getId(), true);  // P4: U2/L1 devuelto
        guardar(l2.getId(), u2.getId(), false); // P5: U2/L2 activo
        guardar(l2.getId(), u2.getId(), true);  // P6: U2/L2 devuelto
    }

    private void guardar(Long libroId, Long usuarioId, boolean devuelto) {
        var prestamo = new Prestamo(libroId, usuarioId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        prestamo.setTipoPrestamo("DOMICILIO");
        prestamo.setDevuelto(devuelto);
        if (devuelto) prestamo.setFechaDevolucion(LocalDate.of(2026, 1, 10));
        idsPrestamos.add(prestamos.saveAndFlush(prestamo).getId());
    }

    private void comprobarIds(String caso, Map<String, String> parametros, int... posiciones) throws Exception {
        var solicitud = get("/api/prestamos");
        parametros.forEach((nombre, valor) -> solicitud.param(nombre, switch (valor) {
            case "U1" -> usuarioUno.toString();
            case "L1" -> libroUno.toString();
            default -> valor;
        }));
        var respuesta = mvc.perform(solicitud).andReturn().getResponse();
        assertEquals(200, respuesta.getStatus(), caso);
        var json = mapper.readTree(respuesta.getContentAsByteArray());
        assertTrue(json.isArray());
        List<Long> observados = new ArrayList<>();
        json.forEach(fila -> observados.add(fila.get("id").longValue()));
        observados.sort(Long::compareTo);
        var esperados = Arrays.stream(posiciones).mapToObj(posicion -> idsPrestamos.get(posicion - 1))
                .sorted().toList();
        System.out.printf("RFC014 CASO=%s; parametros=%s; HTTP=%d; IDs esperados=%s; IDs observados=%s%n",
                caso, parametros,
                respuesta.getStatus(), esperados, observados);
        assertEquals(esperados, observados, caso + ": IDs exactos sin depender del orden");
    }

    @Test
    void controlSinFiltrosDevuelveHistorialCompleto() throws Exception {
        comprobarIds("CONTROL SIN FILTROS", Map.of(), 1, 2, 3, 4, 5, 6);
    }

    static Stream<Arguments> filtrosValidos() {
        return Stream.of(
                Arguments.of("usuario", Map.of("usuarioId", "U1"), new int[]{1, 2, 3}),
                Arguments.of("libro", Map.of("libroId", "L1"), new int[]{1, 2, 4}),
                Arguments.of("devueltos", Map.of("devuelto", "true"), new int[]{2, 3, 4, 6}),
                Arguments.of("activos", Map.of("devuelto", "false"), new int[]{1, 5}),
                Arguments.of("usuario+libro", Map.of("usuarioId", "U1", "libroId", "L1"), new int[]{1, 2}),
                Arguments.of("usuario+devueltos", Map.of("usuarioId", "U1", "devuelto", "true"), new int[]{2, 3}),
                Arguments.of("usuario+activos", Map.of("usuarioId", "U1", "devuelto", "false"), new int[]{1}),
                Arguments.of("libro+devueltos", Map.of("libroId", "L1", "devuelto", "true"), new int[]{2, 4}),
                Arguments.of("libro+activos", Map.of("libroId", "L1", "devuelto", "false"), new int[]{1}),
                Arguments.of("tres+devueltos", Map.of("usuarioId", "U1", "libroId", "L1", "devuelto", "true"), new int[]{2}),
                Arguments.of("tres+activos", Map.of("usuarioId", "U1", "libroId", "L1", "devuelto", "false"), new int[]{1}),
                Arguments.of("sin coincidencias", Map.of("usuarioId", "9223372036854775807"), new int[]{}));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filtrosValidos")
    void filtraPorIdsExactos(String caso, Map<String, String> parametros, int[] posiciones) throws Exception {
        comprobarIds(caso, parametros, posiciones);
    }

    static Stream<Arguments> parametrosInvalidos() {
        return Stream.of(
                Arguments.of("usuarioId", "-1"), Arguments.of("usuarioId", "0"), Arguments.of("usuarioId", "abc"),
                Arguments.of("libroId", "-1"), Arguments.of("libroId", "0"), Arguments.of("libroId", "abc"),
                Arguments.of("devuelto", "quizas"), Arguments.of("devuelto", "2"));
    }

    @ParameterizedTest(name = "{0}={1}")
    @MethodSource("parametrosInvalidos")
    void rechazaParametrosInvalidos(String parametro, String valor) throws Exception {
        var respuesta = mvc.perform(get("/api/prestamos").param(parametro, valor)).andReturn().getResponse();
        System.out.printf("RFC014 INVALIDO=%s=%s; HTTP esperado=400; HTTP observado=%d%n",
                parametro, valor, respuesta.getStatus());
        assertEquals(400, respuesta.getStatus(), parametro + "=" + valor);
    }
}
