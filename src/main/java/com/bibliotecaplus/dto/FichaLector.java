package com.bibliotecaplus.dto;

import java.time.LocalDate;
import java.util.List;

public record FichaLector(
        DatosUsuario usuario,
        Resumen resumen,
        List<DetallePrestamo> historial) {

    public record DatosUsuario(
            Long id,
            String nombre,
            String apellidos,
            String dni,
            String correo,
            String telefono) {
    }

    public record Resumen(
            long totalPrestamos,
            long prestamosActivos,
            long prestamosDevueltos,
            long prestamosVencidos,
            double multasAcumuladas) {
    }

    public record DetallePrestamo(
            Long id,
            Long libroId,
            String libroTitulo,
            LocalDate fechaPrestamo,
            LocalDate fechaVencimiento,
            LocalDate fechaDevolucion,
            String tipoPrestamo,
            boolean devuelto,
            boolean vencido,
            double multa) {
    }
}
