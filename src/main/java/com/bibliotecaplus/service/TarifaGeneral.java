package com.bibliotecaplus.service;

import org.springframework.stereotype.Component;

@Component
public class TarifaGeneral implements TarifaMulta {
    public boolean aplicaA(String tipoPrestamo) { return true; }
    public double valorDiario() { return 0.5; }
}
