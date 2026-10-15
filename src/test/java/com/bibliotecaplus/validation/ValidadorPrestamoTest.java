package com.bibliotecaplus.validation;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Usuario;
import org.junit.jupiter.api.Test;

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
}
