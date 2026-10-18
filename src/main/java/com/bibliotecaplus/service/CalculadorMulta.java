package com.bibliotecaplus.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CalculadorMulta {
    private final List<TarifaMulta> tarifas;

    public CalculadorMulta(List<TarifaMulta> tarifas) {
        this.tarifas = tarifas;
    }

    public double calcular(String tipoPrestamo, long diasRetraso) {
        return seleccionarTarifa(tipoPrestamo).valorDiario() * diasRetraso;
    }

    private TarifaMulta seleccionarTarifa(String tipoPrestamo) {
        return tarifas.stream()
                .filter(tarifa -> !tarifa.esPorDefecto() && tarifa.aplicaA(tipoPrestamo))
                .findFirst()
                .or(() -> tarifas.stream().filter(TarifaMulta::esPorDefecto).findFirst())
                .orElseThrow(() -> new IllegalArgumentException("No existe una tarifa configurada"));
    }
}
