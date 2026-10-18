package com.bibliotecaplus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void elResultadoNoDependeDelOrdenDeLasTarifas() {
        List<TarifaMulta> tarifas = List.of(new TarifaEspecial(), new TarifaDomicilio(), new TarifaGeneral());
        for (int i = 0; i < tarifas.size(); i++) {
            for (int j = 0; j < tarifas.size(); j++) {
                for (int k = 0; k < tarifas.size(); k++) {
                    if (i == j || j == k || i == k) continue;
                    CalculadorMulta calc = new CalculadorMulta(new ArrayList<>(List.of(tarifas.get(i), tarifas.get(j), tarifas.get(k))));
                    assertEquals(2.0, calc.calcular("ESPECIAL", 1));
                    assertEquals(1.0, calc.calcular("DOMICILIO", 1));
                    assertEquals(0.5, calc.calcular("SALA", 1));
                }
            }
        }
    }

    @Test
    void sinRetrasoNoHayMulta() {
        assertEquals(0.0, calculador.calcular("ESPECIAL", 0));
    }

    @Test
    void rechazaCalculoSiNoHayTarifaAplicable() {
        CalculadorMulta sinGeneral = new CalculadorMulta(List.of(new TarifaDomicilio()));
        assertThrows(IllegalArgumentException.class, () -> sinGeneral.calcular("ESPECIAL", 1));
    }
}
