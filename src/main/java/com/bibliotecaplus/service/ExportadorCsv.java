package com.bibliotecaplus.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExportadorCsv {
    private static final String SEPARADOR = ";";
    private static final String ENCABEZADO = "ID;Libro;Usuario;Fecha préstamo;Fecha vencimiento;Devuelto;Multa";
    private static final List<String> COLUMNAS =
            List.of("id", "libro", "usuario", "fechaPrestamo", "fechaVencimiento", "devuelto", "multa");

    public String generar(List<Map<String, Object>> filas) {
        StringBuilder csv = new StringBuilder(ENCABEZADO).append('\n');
        filas.forEach(fila -> csv.append(COLUMNAS.stream()
                .map(columna -> escapar(fila.get(columna)))
                .collect(Collectors.joining(SEPARADOR))).append('\n'));
        return csv.toString();
    }

    String escapar(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = String.valueOf(valor);
        if (valor instanceof String && empiezaComoFormula(texto)) {
            texto = "'" + texto;
        }
        if (texto.contains(SEPARADOR) || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            texto = "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private boolean empiezaComoFormula(String texto) {
        return !texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0;
    }
}
