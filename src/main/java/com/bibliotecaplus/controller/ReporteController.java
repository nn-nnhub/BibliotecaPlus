package com.bibliotecaplus.controller;

import com.bibliotecaplus.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) { this.reporteService = reporteService; }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() { return reporteService.generarResumen(); }

    @GetMapping("/prestamos.csv")
    public ResponseEntity<byte[]> exportarPrestamos() {
        List<Map<String, Object>> filas = reporteService.listarPrestamos();
        StringBuilder csv = new StringBuilder("ID;Libro;Usuario;Fecha préstamo;Fecha vencimiento;Devuelto;Multa\n");
        filas.forEach(fila -> csv.append(fila.get("id")).append(';').append(fila.get("libro")).append(';')
                .append(fila.get("usuario")).append(';').append(fila.get("fechaPrestamo")).append(';')
                .append(fila.get("fechaVencimiento")).append(';').append(fila.get("devuelto")).append(';')
                .append(fila.get("multa")).append('\n'));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-prestamos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8));
    }
}
