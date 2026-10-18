package com.bibliotecaplus.service;

import com.bibliotecaplus.exception.RecursoNoEncontradoException;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.validation.ValidadorPrestamo;
import com.bibliotecaplus.notification.Notificador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final CalculadorMulta calculadorMulta;
    private final ValidadorPrestamo validadorPrestamo;
    private final Notificador notificador;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           LibroRepository libroRepository,
                           UsuarioRepository usuarioRepository,
                           CalculadorMulta calculadorMulta,
                           ValidadorPrestamo validadorPrestamo,
                           Notificador notificador) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.calculadorMulta = calculadorMulta;
        this.validadorPrestamo = validadorPrestamo;
        this.notificador = notificador;
    }

    public List<Prestamo> listarTodos() {
        return prestamoRepository.findAll();
    }

    @Transactional
    public Prestamo registrar(Long libroId, Long usuarioId, String tipoPrestamo, LocalDate fechaVencimiento) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el libro"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));

        if (!libro.isDisponible()) {
            throw new IllegalArgumentException("El libro no está disponible");
        }

        validadorPrestamo.validar(libro, usuario);
        validadorPrestamo.validarVencimiento(fechaVencimiento);

        Prestamo prestamo = new Prestamo(libro.getId(), usuario.getId(), LocalDate.now(), fechaVencimiento);
        prestamo.setTipoPrestamo(normalizarTipo(tipoPrestamo));
        libro.setDisponible(false);
        libroRepository.save(libro);

        Prestamo prestamoGuardado = prestamoRepository.save(prestamo);
        notificador.notificarRegistro(usuario);
        return prestamoGuardado;
    }

    private String normalizarTipo(String tipoPrestamo) {
        return tipoPrestamo == null || tipoPrestamo.isBlank()
                ? Prestamo.TIPO_POR_DEFECTO
                : tipoPrestamo.trim().toUpperCase();
    }

    @Transactional
    public Prestamo devolver(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el préstamo"));

        if (prestamo.isDevuelto()) {
            throw new IllegalArgumentException("El préstamo ya fue devuelto");
        }

        LocalDate fechaDevolucion = LocalDate.now();
        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.setDevuelto(true);
        long diasRetraso = Math.max(0, ChronoUnit.DAYS.between(prestamo.getFechaVencimiento(), fechaDevolucion));
        prestamo.setMulta(calculadorMulta.calcular(prestamo.getTipoPrestamo(), diasRetraso));

        Libro libro = libroRepository.findById(prestamo.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el libro"));
        libro.setDisponible(true);
        libroRepository.save(libro);

        return prestamoRepository.save(prestamo);
    }

}
