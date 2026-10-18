package com.bibliotecaplus.repository;

import com.bibliotecaplus.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    boolean existsByLibroId(Long libroId);

    boolean existsByUsuarioId(Long usuarioId);
}
