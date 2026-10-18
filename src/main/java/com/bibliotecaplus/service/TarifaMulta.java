package com.bibliotecaplus.service;

public interface TarifaMulta {
    boolean aplicaA(String tipoPrestamo);
    double valorDiario();

    default boolean esPorDefecto() {
        return false;
    }
}
