package com.bibliotecaplus.service;

import org.springframework.stereotype.Component;

@Component
public class TarifaDomicilio implements TarifaMulta {
    public boolean aplicaA(String tipoPrestamo) { return "DOMICILIO".equalsIgnoreCase(tipoPrestamo); }
    public double valorDiario() { return 1.0; }
}
