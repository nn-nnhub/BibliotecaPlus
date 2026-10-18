package com.bibliotecaplus.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportadorCsvTest {
    private final ExportadorCsv exportador = new ExportadorCsv();

    @Test
    void dejaSinCambiosElTextoNormalYLosValoresNoTexto() {
        assertEquals("Clean Code", exportador.escapar("Clean Code"));
        assertEquals("5.0", exportador.escapar(5.0));
        assertEquals("-1.0", exportador.escapar(-1.0));
        assertEquals("", exportador.escapar(null));
    }

    @Test
    void neutralizaTextoQueEmpiezaComoFormula() {
        assertEquals("'=1+1", exportador.escapar("=1+1"));
        assertEquals("'+cmd", exportador.escapar("+cmd"));
        assertEquals("'-2", exportador.escapar("-2"));
        assertEquals("'@SUMA(A1)", exportador.escapar("@SUMA(A1)"));
        assertEquals("'\tdato", exportador.escapar("\tdato"));
    }

    @Test
    void encierraEntreComillasLosValoresConSeparadorComillasOSaltosDeLinea() {
        assertEquals("\"a;b\"", exportador.escapar("a;b"));
        assertEquals("\"di \"\"hola\"\"\"", exportador.escapar("di \"hola\""));
        assertEquals("\"línea 1\nlínea 2\"", exportador.escapar("línea 1\nlínea 2"));
    }

    @Test
    void neutralizaYEncierraCuandoAmbosCasosCoinciden() {
        assertEquals("\"'=A;B\"", exportador.escapar("=A;B"));
    }

    @Test
    void generaEncabezadoYUnaFilaPorPrestamoConSieteColumnas() {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("id", 1L);
        fila.put("libro", "=Libro;X");
        fila.put("usuario", "Ana Torres");
        fila.put("fechaPrestamo", "2026-10-17");
        fila.put("fechaVencimiento", "2026-10-31");
        fila.put("devuelto", "No");
        fila.put("multa", 0.0);

        String[] lineas = exportador.generar(List.of(fila)).split("\n");

        assertEquals(2, lineas.length);
        assertEquals("ID;Libro;Usuario;Fecha préstamo;Fecha vencimiento;Devuelto;Multa", lineas[0]);
        assertEquals("1;\"'=Libro;X\";Ana Torres;2026-10-17;2026-10-31;No;0.0", lineas[1]);
    }
}
