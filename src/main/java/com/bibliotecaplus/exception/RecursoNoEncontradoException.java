package com.bibliotecaplus.exception;

public class RecursoNoEncontradoException extends IllegalArgumentException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
