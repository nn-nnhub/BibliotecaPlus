package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.repository.LibroRepository;
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
    @Autowired private LibroRepository libroRepository;
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

    private double multaAlDevolver(String isbn, String tipoRegistrado, int diasRetraso, String tipoEnviadoAlDevolver) throws Exception {
        Libro libro = libroRepository.save(new Libro("Libro " + isbn, "Autor", isbn));
        String respuesta = mvc.perform(post("/api/prestamos")
                        .param("libroId", String.valueOf(libro.getId())).param("usuarioId", "1")
                        .param("tipoPrestamo", tipoRegistrado)
                        .param("fechaVencimiento", LocalDate.now().plusDays(14).toString()))
                .andReturn().getResponse().getContentAsString();
        Long id = mapper.readTree(respuesta).get("id").asLong();

        Prestamo prestamo = prestamoRepository.findById(id).orElseThrow();
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(diasRetraso + 14L));
        prestamo.setFechaVencimiento(LocalDate.now().minusDays(diasRetraso));
        prestamoRepository.save(prestamo);

        mvc.perform(put("/api/prestamos/" + id + "/devolver").param("tipoPrestamo", tipoEnviadoAlDevolver));
        return prestamoRepository.findById(id).orElseThrow().getMulta();
    }

    @Test
    void elPrestamoDomicilioCobraUnSolPorDiaDeRetraso() throws Exception {
        assertEquals(3.0, multaAlDevolver("ISBN-MULTA-DOM", "DOMICILIO", 3, "ESPECIAL"));
    }

    @Test
    void elPrestamoDeSalaCobraLaTarifaGeneral() throws Exception {
        assertEquals(2.0, multaAlDevolver("ISBN-MULTA-SALA", "SALA", 4, "ESPECIAL"));
    }

    @Test
    void elTipoSeInterpretaSinDistinguirMayusculas() throws Exception {
        assertEquals(10.0, multaAlDevolver("ISBN-MULTA-MIN", "especial", 5, "DOMICILIO"));
    }

    @Test
    void devolverATiempoNoGeneraMultaAunqueElTipoSeaEspecial() throws Exception {
        assertEquals(0.0, multaAlDevolver("ISBN-MULTA-CERO", "ESPECIAL", 0, "ESPECIAL"));
    }
}
