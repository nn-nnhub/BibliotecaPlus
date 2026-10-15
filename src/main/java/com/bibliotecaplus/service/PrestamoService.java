package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.validation.ValidadorPrestamo;
import com.bibliotecaplus.notification.Notificador;
import org.springframework.stereotype.Service;

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

    public Prestamo registrar(Long libroId, Long usuarioId, String tipoPrestamo, LocalDate fechaVencimiento) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el libro"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario"));

        if (!libro.isDisponible()) {
            throw new IllegalArgumentException("El libro no está disponible");
        }

        validadorPrestamo.validar(libro, usuario);
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
        }

        Prestamo prestamo = new Prestamo(libro.getId(), usuario.getId(), LocalDate.now(), fechaVencimiento);
        libro.setDisponible(false);
        libroRepository.save(libro);

        Prestamo prestamoGuardado = prestamoRepository.save(prestamo);
        notificador.notificarRegistro(usuario);
        return prestamoGuardado;
    }

    public Prestamo devolver(Long prestamoId, String tipoPrestamo) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el préstamo"));

        if (prestamo.isDevuelto()) {
            throw new IllegalArgumentException("El préstamo ya fue devuelto");
        }

        LocalDate fechaDevolucion = LocalDate.now();
        long diasPrestamo = ChronoUnit.DAYS.between(prestamo.getFechaPrestamo(), fechaDevolucion);
        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.setDevuelto(true);
        long diasRetraso = Math.max(0, ChronoUnit.DAYS.between(prestamo.getFechaVencimiento(), fechaDevolucion));
        prestamo.setMulta(calculadorMulta.calcular(tipoPrestamo, diasRetraso));

        Libro libro = libroRepository.findById(prestamo.getLibroId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el libro"));
        libro.setDisponible(true);
        libroRepository.save(libro);

        return prestamoRepository.save(prestamo);
    }

}
