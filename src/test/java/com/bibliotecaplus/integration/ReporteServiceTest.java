package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:reporte-service;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
class ReporteServiceTest {

    @Autowired private ReporteService reporteService;
    @Autowired private PrestamoRepository prestamoRepository;

    /** Datos iniciales: libros 1 (Clean Code) y 2 (Ingeniería de Software); usuarios 1 (Ana Torres) y 2 (Luis Mendoza). */
    @BeforeEach
    void poblarPrestamos() {
        prestamoRepository.deleteAll();
        guardar(1L, 1L, true, 5.0);
        guardar(1L, 2L, false, 0.0);
        guardar(1L, 1L, true, 2.5);
        guardar(2L, 2L, false, 0.0);
        guardar(2L, 1L, true, 0.0);
        guardar(999L, 888L, false, 0.0); // referencias inexistentes, como en datos históricos
    }

    private void guardar(Long libroId, Long usuarioId, boolean devuelto, double multa) {
        Prestamo prestamo = new Prestamo(libroId, usuarioId, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15));
        prestamo.setDevuelto(devuelto);
        prestamo.setMulta(multa);
        prestamoRepository.save(prestamo);
    }

    @Test
    void listaLosPrestamosConTituloYNombreDeUsuario() {
        List<String> filas = reporteService.listarPrestamos().stream()
                .map(fila -> fila.get("libro") + "|" + fila.get("usuario") + "|" + fila.get("devuelto") + "|" + fila.get("multa"))
                .toList();

        assertEquals(List.of(
                "Clean Code|Ana Torres|Sí|5.0",
                "Clean Code|Luis Mendoza|No|0.0",
                "Clean Code|Ana Torres|Sí|2.5",
                "Ingeniería de Software|Luis Mendoza|No|0.0",
                "Ingeniería de Software|Ana Torres|Sí|0.0",
                "Libro 999|Usuario 888|No|0.0"), filas);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generaElResumenConLosLibrosMasPrestados() {
        Map<String, Object> resumen = reporteService.generarResumen();

        assertEquals(6, resumen.get("totalPrestamos"));
        assertEquals(3L, resumen.get("prestamosActivos"));
        assertEquals(3L, resumen.get("prestamosDevueltos"));
        assertEquals(7.5, (double) resumen.get("totalMultas"), 0.0001);
        List<Map<String, Object>> masPrestados = (List<Map<String, Object>>) resumen.get("librosMasPrestados");
        assertEquals(List.of("Clean Code=3", "Ingeniería de Software=2", "Libro 999=1"), masPrestados.stream()
                .map(fila -> fila.get("libro") + "=" + fila.get("prestamos")).toList());
    }
}
