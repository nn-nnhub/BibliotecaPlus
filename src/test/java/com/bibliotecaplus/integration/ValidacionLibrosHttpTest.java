package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:validacion-libros-http;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
class ValidacionLibrosHttpTest {

    @Autowired private MockMvc mvc;

    private String json(String titulo, String autor, String isbn) {
        return "{\"titulo\":" + titulo + ",\"autor\":" + autor + ",\"isbn\":" + isbn + "}";
    }

    @Test
    void rechazaRegistrarUnLibroSinTituloOSinAutor() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"  \"", "\"Autor\"", "\"9782000000001\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El título es obligatorio"));
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"Título\"", "null", "\"9782000000001\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El autor es obligatorio"));
    }

    @Test
    void unLibroNuevoSiempreQuedaDisponibleAunqueElClienteEnvieOtroValor() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Libro nuevo\",\"autor\":\"Autor\",\"isbn\":\"9782000000002\",\"disponible\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void actualizarUnLibroPrestadoNoLoMarcaComoDisponible() throws Exception {
        mvc.perform(post("/api/prestamos").param("libroId", "1").param("usuarioId", "1")
                .param("fechaVencimiento", "2099-01-01")).andExpect(status().isCreated());

        mvc.perform(put("/api/libros/1").contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"Clean Code (nueva edición)\"", "\"Robert C. Martin\"", "\"9780132350884\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code (nueva edición)"));

        mvc.perform(get("/api/libros/1"))
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    void rechazaActualizarConDatosInvalidosYConservaElLibro() throws Exception {
        mvc.perform(put("/api/libros/2").contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"\"", "\"Ian Sommerville\"", "\"9786071503145\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/libros/2").contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"Título\"", "\"Autor\"", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El ISBN es obligatorio"));

        mvc.perform(get("/api/libros/2"))
                .andExpect(jsonPath("$.titulo").value("Ingeniería de Software"));
    }

    @Test
    void actualizarUnLibroInexistenteResponde404() throws Exception {
        mvc.perform(put("/api/libros/9999").contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"Título\"", "\"Autor\"", "\"9782000000003\"")))
                .andExpect(status().isNotFound());
    }
}
