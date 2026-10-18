package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.validation.ValidadorDatos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {
    @Mock private LibroRepository libroRepository;
    @Mock private PrestamoRepository prestamoRepository;
    private LibroService libroService;

    @BeforeEach
    void setUp() {
        libroService = new LibroService(libroRepository, prestamoRepository, new ValidadorDatos());
    }

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

    private void assertRechazaGuardar(Libro libro, String mensaje) {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> libroService.guardar(libro));
        assertEquals(mensaje, excepcion.getMessage());
        verify(libroRepository, never()).save(any());
    }

    @Test
    void rechazaGuardarSinTitulo() {
        assertRechazaGuardar(new Libro(" ", "Autor", "9780000000003"), "El título es obligatorio");
    }

    @Test
    void rechazaGuardarSinAutor() {
        assertRechazaGuardar(new Libro("Título", null, "9780000000003"), "El autor es obligatorio");
    }

    @Test
    void guardarSiempreRegistraElLibroComoDisponible() {
        Libro libro = new Libro("Título", "Autor", "9780000000004");
        libro.setDisponible(false);
        when(libroRepository.save(libro)).thenReturn(libro);
        assertTrue(libroService.guardar(libro).isDisponible());
    }

    @Test
    void rechazaActualizarSinIsbnTituloOAutor() {
        Libro existente = new Libro("Título", "Autor", "9780000000005");
        when(libroRepository.findById(1L)).thenReturn(Optional.of(existente));
        assertThrows(IllegalArgumentException.class, () -> libroService.actualizar(1L, new Libro("Título", "Autor", null)));
        assertThrows(IllegalArgumentException.class, () -> libroService.actualizar(1L, new Libro("", "Autor", "9780000000006")));
        assertThrows(IllegalArgumentException.class, () -> libroService.actualizar(1L, new Libro("Título", " ", "9780000000006")));
        verify(libroRepository, never()).save(any());
    }

    @Test
    void actualizarNoCambiaLaDisponibilidadDelLibro() {
        Libro prestado = new Libro("Título", "Autor", "9780000000007");
        prestado.setDisponible(false);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(prestado));
        when(libroRepository.existsByIsbnAndIdNot("9780000000007", 1L)).thenReturn(false);
        when(libroRepository.save(prestado)).thenReturn(prestado);
        // JSON sin el campo "disponible": el objeto recibido queda con el valor por defecto (true).
        assertFalse(libroService.actualizar(1L, new Libro("Nuevo título", "Autor", "9780000000007")).isDisponible());
    }

    @Test
    void rechazaEliminarLibroConPrestamosAsociados() {
        Libro existente = new Libro("Título", "Autor", "9780000000009");
        when(libroRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(prestamoRepository.existsByLibroId(1L)).thenReturn(true);
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> libroService.eliminar(1L));
        assertEquals("No se puede eliminar el libro porque tiene préstamos asociados", excepcion.getMessage());
        verify(libroRepository, never()).delete(any());
    }

    @Test
    void eliminaLibroSinPrestamos() {
        Libro existente = new Libro("Título", "Autor", "9780000000009");
        when(libroRepository.findById(1L)).thenReturn(Optional.of(existente));
        libroService.eliminar(1L);
        verify(libroRepository).delete(existente);
    }

    @Test
    void eliminarLibroInexistenteLanzaNoEncontrado() {
        assertThrows(com.bibliotecaplus.exception.RecursoNoEncontradoException.class, () -> libroService.eliminar(9999L));
        verify(libroRepository, never()).delete(any());
    }
}
