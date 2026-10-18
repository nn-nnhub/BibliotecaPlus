package com.bibliotecaplus.validation;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ValidadorPrestamo {
    private final ValidadorDatos validadorDatos;

    public ValidadorPrestamo(ValidadorDatos validadorDatos) {
        this.validadorDatos = validadorDatos;
    }

    public void validarVencimiento(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
        }
        if (fechaVencimiento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a hoy");
        }
    }

    public void validar(Libro libro, Usuario usuario) {
        if (libro == null || validadorDatos.estaVacio(libro.getTitulo())
                || usuario == null || validadorDatos.estaVacio(usuario.getNombre())) {
            throw new IllegalArgumentException("El libro y el usuario son obligatorios");
        }
    }
}
