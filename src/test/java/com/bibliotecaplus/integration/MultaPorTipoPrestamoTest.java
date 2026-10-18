package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:multa-tipo;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
class MultaPorTipoPrestamoTest {

    @Autowired private MockMvc mvc;
    @Autowired private PrestamoRepository prestamoRepository;
    @Autowired private ObjectMapper mapper;

    @Test
    void devolucionUsaElTipoRegistradoAunqueElClienteEnvieOtro() throws Exception {
        String respuesta = mvc.perform(post("/api/prestamos")
                        .param("libroId", "1").param("usuarioId", "1")
                        .param("tipoPrestamo", "ESPECIAL")
                        .param("fechaVencimiento", LocalDate.now().plusDays(14).toString()))
                .andReturn().getResponse().getContentAsString();
        Long id = mapper.readTree(respuesta).get("id").asLong();

        Prestamo prestamo = prestamoRepository.findById(id).orElseThrow();
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(24));
        prestamo.setFechaVencimiento(LocalDate.now().minusDays(10));
        prestamoRepository.save(prestamo);

        // La interfaz envía siempre DOMICILIO al devolver (app.js).
        mvc.perform(put("/api/prestamos/" + id + "/devolver").param("tipoPrestamo", "DOMICILIO"));

        assertEquals(20.0, prestamoRepository.findById(id).orElseThrow().getMulta());
    }
}
