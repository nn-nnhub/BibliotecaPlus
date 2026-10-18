package com.bibliotecaplus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TarifasMultaTest {

    private final TarifaDomicilio domicilio = new TarifaDomicilio();
    private final TarifaEspecial especial = new TarifaEspecial();
    private final TarifaGeneral general = new TarifaGeneral();

    @Test
    void tarifaDomicilioCobraUnSolPorDia() {
        assertEquals(1.0, domicilio.valorDiario());
    }

    @Test
    void tarifaEspecialCobraDosSolesPorDia() {
        assertEquals(2.0, especial.valorDiario());
    }

    @Test
    void tarifaGeneralCobraMedioSolPorDia() {
        assertEquals(0.5, general.valorDiario());
    }

    @Test
    void tarifaDomicilioSoloAplicaAlTipoDomicilioSinImportarMayusculas() {
        assertTrue(domicilio.aplicaA("DOMICILIO"));
        assertTrue(domicilio.aplicaA("domicilio"));
        assertFalse(domicilio.aplicaA("ESPECIAL"));
        assertFalse(domicilio.aplicaA("SALA"));
        assertFalse(domicilio.aplicaA(null));
    }

    @Test
    void tarifaEspecialSoloAplicaAlTipoEspecialSinImportarMayusculas() {
        assertTrue(especial.aplicaA("ESPECIAL"));
        assertTrue(especial.aplicaA("Especial"));
        assertFalse(especial.aplicaA("DOMICILIO"));
        assertFalse(especial.aplicaA("SALA"));
        assertFalse(especial.aplicaA(null));
    }

    @Test
    void tarifaGeneralAplicaACualquierTipo() {
        assertTrue(general.aplicaA("SALA"));
        assertTrue(general.aplicaA("DOMICILIO"));
        assertTrue(general.aplicaA("ESPECIAL"));
        assertTrue(general.aplicaA("XYZ"));
        assertTrue(general.aplicaA(null));
    }

    @Test
    void soloLaTarifaGeneralEsPorDefecto() {
        assertTrue(general.esPorDefecto());
        assertFalse(domicilio.esPorDefecto());
        assertFalse(especial.esPorDefecto());
    }
}

