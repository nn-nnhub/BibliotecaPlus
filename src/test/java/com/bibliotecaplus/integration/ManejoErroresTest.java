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
        "spring.datasource.url=jdbc:h2:mem:manejo-errores;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
class ManejoErroresTest {

    @Autowired private MockMvc mvc;

    @Test
    void libroInexistenteResponde404() throws Exception {
        mvc.perform(get("/api/libros/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el libro con id: 9999"));
    }

    @Test
    void usuarioInexistenteResponde404() throws Exception {
        mvc.perform(get("/api/usuarios/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el usuario con id: 9999"));
    }

    @Test
    void prestamoConLibroInexistenteResponde404ConMensaje() throws Exception {
        mvc.perform(post("/api/prestamos").param("libroId", "9999").param("usuarioId", "1")
                        .param("fechaVencimiento", "2099-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el libro"));
    }

    @Test
    void devolverPrestamoInexistenteResponde404() throws Exception {
        mvc.perform(put("/api/prestamos/9999/devolver"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el préstamo"));
    }

    @Test
    void vencimientoPasadoResponde400ConMensaje() throws Exception {
        mvc.perform(post("/api/prestamos").param("libroId", "1").param("usuarioId", "1")
                        .param("fechaVencimiento", "2000-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha de vencimiento no puede ser anterior a hoy"));
    }

    @Test
    void isbnDuplicadoResponde400ConMensajeDeIsbn() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Otro\",\"autor\":\"Autor\",\"isbn\":\"9780132350884\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El ISBN ya está registrado en otro libro"));
    }

    @Test
    void dniDuplicadoResponde400ConMensajeDeDni() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Carlos\",\"apellidos\":\"Prueba\",\"dni\":\"70000001\","
                                + "\"correo\":\"c@p.local\",\"telefono\":\"999\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El DNI ya está registrado en otro usuario"));
    }
}
