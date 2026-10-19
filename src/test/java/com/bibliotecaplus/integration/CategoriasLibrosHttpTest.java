package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:rfc016-antes;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.h2.console.enabled=false"})
@AutoConfigureMockMvc
class CategoriasLibrosHttpTest {

    @Autowired private MockMvc mvc;
    @Autowired private DataSource dataSource;
    @Autowired private PrestamoRepository prestamos;
    @Autowired private LibroRepository libros;
    @Autowired private UsuarioRepository usuarios;

    @BeforeEach
    void prepararBaseAislada() throws Exception {
        try (Connection conexion = dataSource.getConnection()) {
            assertEquals("jdbc:h2:mem:rfc016-antes", conexion.getMetaData().getURL());
        }
        prestamos.deleteAll();
        libros.deleteAll();
        usuarios.deleteAll();
    }

    @Test
    void registraUnLibroConCategoria() throws Exception {
        mvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(libroJson("Fundación", "Isaac Asimov", "RFC016-001", "Ciencia ficción")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("Ciencia ficción"));
    }

    @Test
    void consultaLibrosPorCategoriaSinDistinguirMayusculas() throws Exception {
        registrar("Libro tecnológico", "RFC016-002", "Tecnología");
        registrar("Libro histórico", "RFC016-003", "Historia");

        mvc.perform(get("/api/libros").param("categoria", "tecnología"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Libro tecnológico"));
    }

    @Test
    void muestraSinCategoriaParaUnRegistroAntiguo() throws Exception {
        Libro antiguo = libros.save(new Libro("Libro heredado", "Autor", "RFC016-004"));

        mvc.perform(get("/api/libros/{id}", antiguo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Sin categoría"));
    }

    @Test
    void categoriaVaciaSeMuestraYSePuedeFiltrarComoSinCategoria() throws Exception {
        registrar("Libro sin clasificar", "RFC016-011", "   ");

        mvc.perform(get("/api/libros").param("categoria", "Sin categoría"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria").value("Sin categoría"));
    }

    @Test
    void normalizaEspaciosDeLaCategoria() throws Exception {
        mvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(libroJson("Patrones", "Autor", "RFC016-005", "  Ciencia ficción  ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("Ciencia ficción"));
    }

    @Test
    void rechazaUnaCategoriaDemasiadoLarga() throws Exception {
        String categoria = "A".repeat(101);

        mvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(libroJson("Libro", "Autor", "RFC016-006", categoria)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La categoría no puede superar 100 caracteres"));
    }

    @Test
    void conservaCompatibilidadCuandoElClienteOmiteLaCategoria() throws Exception {
        mvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Cliente anterior\",\"autor\":\"Autor\",\"isbn\":\"RFC016-007\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Cliente anterior"))
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void actualizaCategoriaSinCambiarDisponibilidad() throws Exception {
        Libro prestado = libros.save(new Libro("Libro prestado", "Autor", "RFC016-009"));
        prestado.setDisponible(false);
        libros.saveAndFlush(prestado);

        mvc.perform(put("/api/libros/{id}", prestado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(libroJson("Libro prestado", "Autor", "RFC016-009", "  Historia  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Historia"))
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    void clienteAnteriorPuedeActualizarSinBorrarLaCategoria() throws Exception {
        Libro libro = new Libro("Libro", "Autor", "RFC016-010");
        libro.setCategoria("Tecnología");
        libro = libros.saveAndFlush(libro);

        mvc.perform(put("/api/libros/{id}", libro.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Libro actualizado\",\"autor\":\"Autor\",\"isbn\":\"RFC016-010\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Libro actualizado"))
                .andExpect(jsonPath("$.categoria").value("Tecnología"));
    }

    @Test
    void prestamosYDisponibilidadMantienenSuComportamiento() throws Exception {
        Libro libro = libros.save(new Libro("Libro prestable", "Autor", "RFC016-008"));
        Usuario usuario = usuarios.save(new Usuario(
                "Ana", "Torres", "71600001", "ana.rfc016@example.com", "999111222"));

        mvc.perform(post("/api/prestamos")
                        .param("libroId", libro.getId().toString())
                        .param("usuarioId", usuario.getId().toString())
                        .param("fechaVencimiento", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/libros/{id}", libro.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));
    }

    private void registrar(String titulo, String isbn, String categoria) throws Exception {
        mvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(libroJson(titulo, "Autor", isbn, categoria)))
                .andExpect(status().isCreated());
    }

    private String libroJson(String titulo, String autor, String isbn, String categoria) {
        return "{\"titulo\":\"" + titulo + "\",\"autor\":\"" + autor
                + "\",\"isbn\":\"" + isbn + "\",\"categoria\":\"" + categoria + "\"}";
    }
}
