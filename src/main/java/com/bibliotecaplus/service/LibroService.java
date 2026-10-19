package com.bibliotecaplus.service;

import com.bibliotecaplus.exception.RecursoNoEncontradoException;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.validation.ValidadorDatos;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final ValidadorDatos validadorDatos;

    public LibroService(LibroRepository libroRepository, PrestamoRepository prestamoRepository, ValidadorDatos validadorDatos) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.validadorDatos = validadorDatos;
    }

    public List<Libro> listarTodos() {
        return libroRepository.findAll();
    }

    public List<Libro> listarPorCategoria(String categoria) {
        String categoriaNormalizada = normalizarCategoria(categoria);
        if (categoriaNormalizada == null) {
            return listarTodos();
        }
        if (Libro.SIN_CATEGORIA.equalsIgnoreCase(categoriaNormalizada)) {
            return libroRepository.findSinCategoria();
        }
        return libroRepository.findByCategoriaIgnoreCase(categoriaNormalizada);
    }

    public Libro buscarPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el libro con id: " + id));
    }

    public Libro guardar(Libro libro) {
        validarTituloYAutor(libro);
        validarIsbnDisponible(libro.getIsbn());
        libro.setCategoria(normalizarCategoria(libro.getCategoriaAlmacenada()));
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
        if (datosLibro.getCategoriaAlmacenada() != null) {
            libro.setCategoria(normalizarCategoria(datosLibro.getCategoriaAlmacenada()));
        }
        return libroRepository.save(libro);
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null) {
            return null;
        }
        String normalizada = categoria.trim();
        if (normalizada.isEmpty()) {
            return null;
        }
        if (normalizada.length() > 100) {
            throw new IllegalArgumentException("La categoría no puede superar 100 caracteres");
        }
        return normalizada;
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
        Libro libro = buscarPorId(id);
        if (prestamoRepository.existsByLibroId(id)) {
            throw new IllegalArgumentException("No se puede eliminar el libro porque tiene préstamos asociados");
        }
        libroRepository.delete(libro);
    }
}
