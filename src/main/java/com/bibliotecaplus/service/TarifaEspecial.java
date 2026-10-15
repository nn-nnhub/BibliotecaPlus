package com.bibliotecaplus.service;

import org.springframework.stereotype.Component;

@Component
public class TarifaEspecial implements TarifaMulta {
    public boolean aplicaA(String tipoPrestamo) { return "ESPECIAL".equalsIgnoreCase(tipoPrestamo); }
    public double valorDiario() { return 2.0; }
}
