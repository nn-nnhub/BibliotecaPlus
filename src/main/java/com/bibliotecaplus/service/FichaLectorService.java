package com.bibliotecaplus.service;

import com.bibliotecaplus.dto.FichaLector;
import com.bibliotecaplus.exception.RecursoNoEncontradoException;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FichaLectorService {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;

    public FichaLectorService(UsuarioRepository usuarioRepository,
                              PrestamoRepository prestamoRepository,
                              LibroRepository libroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public FichaLector obtener(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario con id: " + usuarioId));
        var prestamos = prestamoRepository.findByUsuarioIdOrderByFechaPrestamoDesc(usuarioId);
        Map<Long, Libro> libros = libroRepository.findAllById(prestamos.stream()
                        .map(Prestamo::getLibroId)
                        .distinct()
                        .toList()).stream()
                .collect(Collectors.toMap(Libro::getId, Function.identity()));
        LocalDate hoy = LocalDate.now();

        var historial = prestamos.stream()
                .map(prestamo -> detalle(prestamo, libros.get(prestamo.getLibroId()), hoy))
                .toList();
        long activos = prestamos.stream().filter(prestamo -> !prestamo.isDevuelto()).count();
        long devueltos = prestamos.size() - activos;
        long vencidos = prestamos.stream().filter(prestamo -> estaVencido(prestamo, hoy)).count();
        double multas = prestamos.stream().mapToDouble(Prestamo::getMulta).sum();

        return new FichaLector(
                new FichaLector.DatosUsuario(usuario.getId(), usuario.getNombre(), usuario.getApellidos(),
                        usuario.getDni(), usuario.getCorreo(), usuario.getTelefono()),
                new FichaLector.Resumen(prestamos.size(), activos, devueltos, vencidos, multas),
                historial);
    }

    private FichaLector.DetallePrestamo detalle(Prestamo prestamo, Libro libro, LocalDate hoy) {
        String titulo = libro == null ? "Libro " + prestamo.getLibroId() : libro.getTitulo();
        return new FichaLector.DetallePrestamo(
                prestamo.getId(), prestamo.getLibroId(), titulo, prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(), prestamo.getFechaDevolucion(), prestamo.getTipoPrestamo(),
                prestamo.isDevuelto(), estaVencido(prestamo, hoy), prestamo.getMulta());
    }

    private boolean estaVencido(Prestamo prestamo, LocalDate hoy) {
        return !prestamo.isDevuelto() && prestamo.getFechaVencimiento() != null
                && prestamo.getFechaVencimiento().isBefore(hoy);
    }
}
