package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:reporte-controller;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
class ReporteControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private PrestamoRepository prestamoRepository;

    @BeforeEach
    void limpiarPrestamos() {
        prestamoRepository.deleteAll();
    }

    private void registrarPrestamo(String libroId, String usuarioId) throws Exception {
        mvc.perform(post("/api/prestamos").param("libroId", libroId).param("usuarioId", usuarioId)
                .param("tipoPrestamo", "DOMICILIO").param("fechaVencimiento", "2099-01-01"))
                .andExpect(status().isCreated());
    }

    @Test
    void elResumenInformaLosTotalesYElLibroMasPrestado() throws Exception {
        registrarPrestamo("1", "1");

        mvc.perform(get("/api/reportes/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrestamos").value(1))
                .andExpect(jsonPath("$.prestamosActivos").value(1))
                .andExpect(jsonPath("$.prestamosDevueltos").value(0))
                .andExpect(jsonPath("$.totalMultas").value(0.0))
                .andExpect(jsonPath("$.librosMasPrestados[0].libro").value("Clean Code"))
                .andExpect(jsonPath("$.librosMasPrestados[0].prestamos").value(1));
    }

    @Test
    void elCsvSeDescargaConBomEncabezadoYCabecerasEsperadas() throws Exception {
        registrarPrestamo("2", "2");

        var respuesta = mvc.perform(get("/api/reportes/prestamos.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=reporte-prestamos.csv"))
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andReturn().getResponse();

        String csv = new String(respuesta.getContentAsByteArray(), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFFID;Libro;Usuario;Fecha préstamo;Fecha vencimiento;Devuelto;Multa\n"), csv);
        assertTrue(csv.contains(";Ingeniería de Software;Luis Mendoza;"), csv);
        assertEquals(2, csv.strip().lines().count());
    }
}
