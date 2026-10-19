package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:rfc017-antes;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.h2.console.enabled=false"})
@AutoConfigureMockMvc
class FichaLectorHttpTest {

    @Autowired private MockMvc mvc;
    @Autowired private DataSource dataSource;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private LibroRepository libros;
    @Autowired private PrestamoRepository prestamos;

    @BeforeEach
    void prepararBaseAislada() throws Exception {
        try (Connection conexion = dataSource.getConnection()) {
            assertEquals("jdbc:h2:mem:rfc017-antes", conexion.getMetaData().getURL());
        }
        prestamos.deleteAll();
        libros.deleteAll();
        usuarios.deleteAll();
    }

    @Test
    void controlEndpointActualDeUsuarioSigueDisponible() throws Exception {
        Usuario usuario = guardarUsuario("Control", "71610000");

        mvc.perform(get("/api/usuarios/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("71610000"));
    }

    @Test
    void usuarioInexistenteResponde404Controlado() throws Exception {
        mvc.perform(get("/api/usuarios/999999/ficha"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el usuario con id: 999999"));
    }

    @Test
    void lectorSinPrestamosDevuelveFichaYResumenEnCero() throws Exception {
        Usuario usuario = guardarUsuario("Sin prestamos", "71610001");

        mvc.perform(get("/api/usuarios/{id}/ficha", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.id").value(usuario.getId()))
                .andExpect(jsonPath("$.usuario.nombre").value("Sin prestamos"))
                .andExpect(jsonPath("$.resumen.totalPrestamos").value(0))
                .andExpect(jsonPath("$.resumen.prestamosActivos").value(0))
                .andExpect(jsonPath("$.resumen.prestamosDevueltos").value(0))
                .andExpect(jsonPath("$.resumen.prestamosVencidos").value(0))
                .andExpect(jsonPath("$.resumen.multasAcumuladas").value(0.0))
                .andExpect(jsonPath("$.historial", hasSize(0)));
    }

    @Test
    void consolidaPrestamosActivosDevueltosVencidosYMultas() throws Exception {
        Escenario escenario = guardarEscenarioCompleto();

        mvc.perform(get("/api/usuarios/{id}/ficha", escenario.usuario().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.totalPrestamos").value(4))
                .andExpect(jsonPath("$.resumen.prestamosActivos").value(2))
                .andExpect(jsonPath("$.resumen.prestamosDevueltos").value(2))
                .andExpect(jsonPath("$.resumen.prestamosVencidos").value(1))
                .andExpect(jsonPath("$.resumen.multasAcumuladas").value(8.0))
                .andExpect(jsonPath("$.historial", hasSize(4)));
    }

    @Test
    void historialIncluyeLibroFechasTipoEstadoYMulta() throws Exception {
        Escenario escenario = guardarEscenarioCompleto();

        mvc.perform(get("/api/usuarios/{id}/ficha", escenario.usuario().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historial[0].libroId").value(escenario.libroActivo().getId()))
                .andExpect(jsonPath("$.historial[0].libroTitulo").value("Libro activo"))
                .andExpect(jsonPath("$.historial[0].fechaPrestamo").value(LocalDate.now().minusDays(2).toString()))
                .andExpect(jsonPath("$.historial[0].fechaVencimiento").value(LocalDate.now().plusDays(5).toString()))
                .andExpect(jsonPath("$.historial[0].tipoPrestamo").value("DOMICILIO"))
                .andExpect(jsonPath("$.historial[0].devuelto").value(false))
                .andExpect(jsonPath("$.historial[0].vencido").value(false))
                .andExpect(jsonPath("$.historial[0].multa").value(0.0));
    }

    @Test
    void prestamoActivoPasadoSeMarcaVencidoSinInventarMulta() throws Exception {
        Escenario escenario = guardarEscenarioCompleto();

        mvc.perform(get("/api/usuarios/{id}/ficha", escenario.usuario().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historial[1].libroTitulo").value("Libro vencido"))
                .andExpect(jsonPath("$.historial[1].devuelto").value(false))
                .andExpect(jsonPath("$.historial[1].vencido").value(true))
                .andExpect(jsonPath("$.historial[1].multa").value(0.0));
    }

    @Test
    void multaAcumuladaUsaLosImportesPersistidosDePrestamosDevueltos() throws Exception {
        Escenario escenario = guardarEscenarioCompleto();

        mvc.perform(get("/api/usuarios/{id}/ficha", escenario.usuario().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.multasAcumuladas").value(8.0))
                .andExpect(jsonPath("$.historial[2].multa").value(8.0))
                .andExpect(jsonPath("$.historial[3].multa").value(0.0));
    }

    @Test
    void consultarFichaNoModificaUsuarioLibrosNiPrestamos() throws Exception {
        Escenario escenario = guardarEscenarioCompleto();
        long usuariosAntes = usuarios.count();
        long librosAntes = libros.count();
        long prestamosAntes = prestamos.count();
        boolean disponibilidadAntes = escenario.libroActivo().isDisponible();
        var respuesta = mvc.perform(get("/api/usuarios/{id}/ficha", escenario.usuario().getId())).andReturn();

        assertAll(
                () -> assertEquals(200, respuesta.getResponse().getStatus()),
                () -> assertEquals(usuariosAntes, usuarios.count()),
                () -> assertEquals(librosAntes, libros.count()),
                () -> assertEquals(prestamosAntes, prestamos.count()),
                () -> assertEquals(disponibilidadAntes,
                        libros.findById(escenario.libroActivo().getId()).orElseThrow().isDisponible()),
                () -> assertEquals(8.0, prestamos.findById(escenario.prestamoConMulta().getId()).orElseThrow().getMulta()));
    }

    private Usuario guardarUsuario(String nombre, String dni) {
        return usuarios.save(new Usuario(nombre, "Lector", dni, nombre.replace(" ", ".") + "@example.com", "999111222"));
    }

    private Escenario guardarEscenarioCompleto() {
        Usuario usuario = guardarUsuario("Ana RFC017", "71610002");
        Libro activo = libros.save(new Libro("Libro activo", "Autor", "RFC017-A"));
        Libro vencido = libros.save(new Libro("Libro vencido", "Autor", "RFC017-B"));
        Libro multado = libros.save(new Libro("Libro devuelto tarde", "Autor", "RFC017-C"));
        Libro devuelto = libros.save(new Libro("Libro devuelto a tiempo", "Autor", "RFC017-D"));

        Prestamo p1 = prestamo(activo, usuario, LocalDate.now().minusDays(2), LocalDate.now().plusDays(5));
        Prestamo p2 = prestamo(vencido, usuario, LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));
        Prestamo p3 = prestamo(multado, usuario, LocalDate.now().minusDays(30), LocalDate.now().minusDays(15));
        p3.setDevuelto(true);
        p3.setFechaDevolucion(LocalDate.now().minusDays(11));
        p3.setMulta(8.0);
        Prestamo p4 = prestamo(devuelto, usuario, LocalDate.now().minusDays(40), LocalDate.now().minusDays(25));
        p4.setDevuelto(true);
        p4.setFechaDevolucion(LocalDate.now().minusDays(26));

        prestamos.save(p1);
        prestamos.save(p2);
        p3 = prestamos.save(p3);
        prestamos.save(p4);
        return new Escenario(usuario, activo, p3);
    }

    private Prestamo prestamo(Libro libro, Usuario usuario, LocalDate inicio, LocalDate vencimiento) {
        Prestamo prestamo = new Prestamo(libro.getId(), usuario.getId(), inicio, vencimiento);
        prestamo.setTipoPrestamo("DOMICILIO");
        return prestamo;
    }

    private record Escenario(Usuario usuario, Libro libroActivo, Prestamo prestamoConMulta) {
    }
}
