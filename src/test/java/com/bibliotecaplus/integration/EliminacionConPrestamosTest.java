package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:eliminacion;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
class EliminacionConPrestamosTest {

    @Autowired private MockMvc mvc;
    @Autowired private LibroRepository libroRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PrestamoRepository prestamoRepository;

    @Test
    void noEliminaLibroNiUsuarioConPrestamosYConservaElHistorial() throws Exception {
        mvc.perform(post("/api/prestamos").param("libroId", "1").param("usuarioId", "1")
                        .param("fechaVencimiento", "2099-01-01"))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/libros/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede eliminar el libro porque tiene préstamos asociados"));
        mvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede eliminar el usuario porque tiene préstamos asociados"));

        assertTrue(libroRepository.existsById(1L));
        assertTrue(usuarioRepository.existsById(1L));
        assertEquals(1, prestamoRepository.count());
    }

    @Test
    void eliminaLibroYUsuarioSinPrestamos() throws Exception {
        mvc.perform(delete("/api/libros/2")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/usuarios/2")).andExpect(status().isNoContent());
    }

    @Test
    void eliminarUnRecursoInexistenteResponde404() throws Exception {
        mvc.perform(delete("/api/libros/9999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/usuarios/9999")).andExpect(status().isNotFound());
    }
}
