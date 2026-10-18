package com.bibliotecaplus.controller;

import com.bibliotecaplus.service.ExportadorCsv;
import com.bibliotecaplus.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService reporteService;
    private final ExportadorCsv exportadorCsv;

    public ReporteController(ReporteService reporteService, ExportadorCsv exportadorCsv) {
        this.reporteService = reporteService;
        this.exportadorCsv = exportadorCsv;
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() { return reporteService.generarResumen(); }

    @GetMapping("/prestamos.csv")
    public ResponseEntity<byte[]> exportarPrestamos() {
        String csv = exportadorCsv.generar(reporteService.listarPrestamos());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-prestamos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8));
    }
}
