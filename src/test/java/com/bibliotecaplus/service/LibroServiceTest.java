package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {
    @Mock private LibroRepository libroRepository;
    @InjectMocks private LibroService libroService;

    @Test
    void rechazaIsbnDuplicado() {
        Libro libro = new Libro("Libro de prueba", "Autor de prueba", "9780000000001");
        when(libroRepository.existsByIsbn("9780000000001")).thenReturn(true);
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> libroService.guardar(libro));
        assertEquals("El ISBN ya está registrado en otro libro", excepcion.getMessage());
    }

    @Test
    void aceptaIsbnDisponible() {
        Libro libro = new Libro("Libro de prueba", "Autor de prueba", "9780000000002");
        when(libroRepository.existsByIsbn("9780000000002")).thenReturn(false);
        when(libroRepository.save(libro)).thenReturn(libro);
        assertEquals("9780000000002", libroService.guardar(libro).getIsbn());
    }
}
