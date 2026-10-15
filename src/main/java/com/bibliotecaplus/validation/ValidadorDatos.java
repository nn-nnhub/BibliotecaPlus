package com.bibliotecaplus.validation;

import org.springframework.stereotype.Component;

@Component
public class ValidadorDatos {
    public boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    public void exigirTexto(String valor, String mensaje) {
        if (estaVacio(valor)) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
