package com.bibliotecaplus.exception;

public class ConflictoBloqueoLibroException extends RuntimeException {
    public ConflictoBloqueoLibroException(Throwable causa) {
        super("El libro está siendo procesado por otra solicitud. Inténtalo de nuevo.", causa);
    }
}
