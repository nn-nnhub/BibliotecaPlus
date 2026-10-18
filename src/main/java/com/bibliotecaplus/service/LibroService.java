package com.bibliotecaplus.service;

import com.bibliotecaplus.exception.RecursoNoEncontradoException;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.validation.ValidadorDatos;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final ValidadorDatos validadorDatos;

    public LibroService(LibroRepository libroRepository, ValidadorDatos validadorDatos) {
        this.libroRepository = libroRepository;
        this.validadorDatos = validadorDatos;
    }

    public List<Libro> listarTodos() {
        return libroRepository.findAll();
    }

    public Libro buscarPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el libro con id: " + id));
    }

    public Libro guardar(Libro libro) {
        validarTituloYAutor(libro);
        validarIsbnDisponible(libro.getIsbn());
        libro.setDisponible(true);
        return libroRepository.save(libro);
    }

    public Libro actualizar(Long id, Libro datosLibro) {
        Libro libro = buscarPorId(id);
        validarTituloYAutor(datosLibro);
        validadorDatos.exigirTexto(datosLibro.getIsbn(), "El ISBN es obligatorio");
        if (libroRepository.existsByIsbnAndIdNot(datosLibro.getIsbn(), id)) {
            throw new IllegalArgumentException("El ISBN ya está registrado en otro libro");
        }
        libro.setTitulo(datosLibro.getTitulo());
        libro.setAutor(datosLibro.getAutor());
        libro.setIsbn(datosLibro.getIsbn());
        return libroRepository.save(libro);
    }

    private void validarTituloYAutor(Libro libro) {
        validadorDatos.exigirTexto(libro.getTitulo(), "El título es obligatorio");
        validadorDatos.exigirTexto(libro.getAutor(), "El autor es obligatorio");
    }

    private void validarIsbnDisponible(String isbn) {
        validadorDatos.exigirTexto(isbn, "El ISBN es obligatorio");
        if (libroRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("El ISBN ya está registrado en otro libro");
        }
    }

    public void eliminar(Long id) {
        libroRepository.deleteById(id);
    }
}
