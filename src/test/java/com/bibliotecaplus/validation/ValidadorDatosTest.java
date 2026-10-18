package com.bibliotecaplus.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorDatosTest {
    private final ValidadorDatos validador = new ValidadorDatos();

    @Test
    void aceptaDniDeOchoDigitos() {
        assertTrue(validador.esDniValido("70000001"));
    }

    @Test
    void rechazaDniNuloOConFormatoIncorrecto() {
        assertFalse(validador.esDniValido(null));
        assertFalse(validador.esDniValido(""));
        assertFalse(validador.esDniValido("1234567"));
        assertFalse(validador.esDniValido("123456789"));
        assertFalse(validador.esDniValido("12345a78"));
    }

    @Test
    void aceptaCorreosConFormatoValido() {
        assertTrue(validador.esCorreoValido("ana.torres@bibliotecaplus.com"));
        assertTrue(validador.esCorreoValido("a+b@dominio.pe"));
    }

    @Test
    void rechazaCorreosNulosOMalFormados() {
        assertFalse(validador.esCorreoValido(null));
        assertFalse(validador.esCorreoValido("sin-arroba"));
        assertFalse(validador.esCorreoValido("usuario@sinpunto"));
        assertFalse(validador.esCorreoValido("con espacio@dominio.pe"));
    }
}
