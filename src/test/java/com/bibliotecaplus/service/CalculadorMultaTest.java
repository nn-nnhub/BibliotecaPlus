package com.bibliotecaplus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

class CalculadorMultaTest {

    private final CalculadorMulta calculador = new CalculadorMulta(List.of(new TarifaEspecial(), new TarifaDomicilio(), new TarifaGeneral()));

    @Test
    void calculaTarifaDomicilio() {
        assertEquals(5.0, calculador.calcular("DOMICILIO", 5));
    }

    @Test
    void calculaTarifaEspecial() {
        assertEquals(10.0, calculador.calcular("ESPECIAL", 5));
    }

    @Test
    void usaTarifaGeneralParaOtroTipo() {
        assertEquals(2.5, calculador.calcular("SALA", 5));
    }
}
