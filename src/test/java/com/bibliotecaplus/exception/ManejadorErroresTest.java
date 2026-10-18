package com.bibliotecaplus.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManejadorErroresTest {
    private final ManejadorErrores manejador = new ManejadorErrores();

    private DataIntegrityViolationException violacion(String detalle) {
        return new DataIntegrityViolationException("error", new RuntimeException(detalle));
    }

    @Test
    void identificaLaRestriccionDeIsbn() {
        assertEquals("El ISBN ya está registrado en otro libro",
                manejador.describirRestriccion(violacion("Unique index violation: PUBLIC.LIBRO(ISBN)")));
    }

    @Test
    void identificaLaRestriccionDeDni() {
        assertEquals("El DNI ya está registrado en otro usuario",
                manejador.describirRestriccion(violacion("Unique index violation: PUBLIC.USUARIO(DNI)")));
    }

    @Test
    void usaMensajeGenericoParaOtrasRestricciones() {
        assertEquals("Los datos enviados incumplen una restricción de integridad",
                manejador.describirRestriccion(violacion("NULL not allowed for column TITULO")));
    }
}
