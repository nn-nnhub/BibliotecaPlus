package com.bibliotecaplus.repository;

import com.bibliotecaplus.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Query("select p from Prestamo p where (:usuarioId is null or p.usuarioId = :usuarioId) "
            + "and (:libroId is null or p.libroId = :libroId) "
            + "and (:devuelto is null or p.devuelto = :devuelto)")
    List<Prestamo> buscarConFiltros(@Param("usuarioId") Long usuarioId,
                                  @Param("libroId") Long libroId,
                                  @Param("devuelto") Boolean devuelto);

    boolean existsByLibroId(Long libroId);

    boolean existsByUsuarioId(Long usuarioId);

    List<Prestamo> findByUsuarioIdOrderByFechaPrestamoDesc(Long usuarioId);
}
