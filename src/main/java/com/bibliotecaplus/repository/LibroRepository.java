package com.bibliotecaplus.repository;

import com.bibliotecaplus.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    List<Libro> findByCategoriaIgnoreCase(String categoria);

    @Query("select l from Libro l where l.categoria is null or trim(l.categoria) = ''")
    List<Libro> findSinCategoria();
}
