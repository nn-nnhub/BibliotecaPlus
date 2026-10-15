package com.bibliotecaplus.service;

import org.springframework.stereotype.Component;

@Component
public class CalculadorMulta {

    public double calcular(String tipoPrestamo, long diasRetraso) {
        double tarifaDiaria = obtenerTarifa(tipoPrestamo);
        return diasRetraso * tarifaDiaria;
    }

    private double obtenerTarifa(String tipoPrestamo) {
        if ("ESPECIAL".equalsIgnoreCase(tipoPrestamo)) {
            return 2.0;
        }
        if ("DOMICILIO".equalsIgnoreCase(tipoPrestamo)) {
            return 1.0;
        }
        return 0.5;
    }
}
