package com.bibliotecaplus.validation;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorPrestamoTest {
    private final ValidadorPrestamo validador = new ValidadorPrestamo(new ValidadorDatos());

    @Test
    void aceptaLibroYUsuarioValidos() {
        assertDoesNotThrow(() -> validador.validar(new Libro("Clean Code", "Robert C. Martin", "9780132350884"),
                new Usuario("Ana", "Torres", "70000001", "ana@biblioteca.local", "999111222")));
    }

    @Test
    void rechazaLibroSinTitulo() {
        assertThrows(IllegalArgumentException.class, () -> validador.validar(new Libro("", "Autor", "9780000000001"),
                new Usuario("Ana", "Torres", "70000001", "ana@biblioteca.local", "999111222")));
    }

    @Test
    void rechazaVencimientoNulo() {
        assertThrows(IllegalArgumentException.class, () -> validador.validarVencimiento(null));
    }

    @Test
    void rechazaVencimientoAnteriorAHoy() {
        assertThrows(IllegalArgumentException.class, () -> validador.validarVencimiento(LocalDate.now().minusDays(1)));
    }

    @Test
    void aceptaVencimientoDeHoyYFuturo() {
        assertDoesNotThrow(() -> validador.validarVencimiento(LocalDate.now()));
        assertDoesNotThrow(() -> validador.validarVencimiento(LocalDate.now().plusDays(14)));
    }
}
