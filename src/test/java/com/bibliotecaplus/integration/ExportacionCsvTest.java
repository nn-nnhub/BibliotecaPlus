package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:exportacion-csv;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
class ExportacionCsvTest {

    @Autowired private MockMvc mvc;

    @Test
    void neutralizaFormulasYEscapaSeparadoresEnElCsv() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"=HYPERLINK(\\\"x\\\";\\\"y\\\")\",\"autor\":\"Autor\",\"isbn\":\"9781111111111\"}"))
                .andReturn();
        mvc.perform(post("/api/prestamos").param("libroId", "3").param("usuarioId", "1")
                .param("fechaVencimiento", "2099-01-01")).andReturn();

        String csv = new String(mvc.perform(get("/api/reportes/prestamos.csv")).andReturn()
                .getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        assertTrue(csv.contains(";\"'=HYPERLINK(\"\"x\"\";\"\"y\"\")\";Ana Torres;"), csv);
    }
}
