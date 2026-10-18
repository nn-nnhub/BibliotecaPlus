package com.bibliotecaplus.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ManejadorErrores {
    @ExceptionHandler(ConflictoBloqueoLibroException.class)
    public ResponseEntity<Map<String, String>> manejarBloqueo(ConflictoBloqueoLibroException excepcion) {
        return respuesta(HttpStatus.CONFLICT, excepcion.getMessage());
    }


    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RecursoNoEncontradoException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(IllegalArgumentException excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarRestriccion(DataIntegrityViolationException excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, describirRestriccion(excepcion));
    }

    String describirRestriccion(DataIntegrityViolationException excepcion) {
        String detalle = String.valueOf(excepcion.getMostSpecificCause().getMessage()).toUpperCase();
        if (detalle.contains("ISBN")) {
            return "El ISBN ya está registrado en otro libro";
        }
        if (detalle.contains("DNI")) {
            return "El DNI ya está registrado en otro usuario";
        }
        return "Los datos enviados incumplen una restricción de integridad";
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("message", mensaje));
    }
}
