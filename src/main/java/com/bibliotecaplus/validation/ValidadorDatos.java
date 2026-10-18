package com.bibliotecaplus.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidadorDatos {
    private static final Pattern DNI = Pattern.compile("\\d{8}");
    private static final Pattern CORREO = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");

    public boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    public boolean esDniValido(String dni) {
        return dni != null && DNI.matcher(dni).matches();
    }

    public boolean esCorreoValido(String correo) {
        return correo != null && CORREO.matcher(correo).matches();
    }

    public void exigirTexto(String valor, String mensaje) {
        if (estaVacio(valor)) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
