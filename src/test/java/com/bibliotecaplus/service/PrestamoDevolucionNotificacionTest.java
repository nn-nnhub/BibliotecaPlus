package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.notification.Notificador;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.validation.ValidadorPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrestamoDevolucionNotificacionTest {

    private PrestamoRepository prestamoRepository;
    private LibroRepository libroRepository;
    private UsuarioRepository usuarioRepository;
    private CalculadorMulta calculadorMulta;
    private Notificador notificador;
    private PrestamoService servicio;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        prestamoRepository = mock(PrestamoRepository.class);
        libroRepository = mock(LibroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        calculadorMulta = mock(CalculadorMulta.class);
        notificador = mock(Notificador.class);
        servicio = new PrestamoService(prestamoRepository, libroRepository, usuarioRepository,
                calculadorMulta, mock(ValidadorPrestamo.class), notificador);

        usuario = new Usuario("Ana", "Torres", "70000001", "ana.torres@bibliotecaplus.com", "999111222");
        when(libroRepository.findById(1L)).thenReturn(Optional.of(new Libro("Clean Code", "Robert C. Martin", "9780132350884")));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void prestamoConVencimiento(LocalDate vencimiento) {
        Prestamo prestamo = new Prestamo(1L, 2L, LocalDate.now().minusDays(10), vencimiento);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
    }

    @Test
    void devolucionATiempoNotificaSoloLaDevolucion() {
        prestamoConVencimiento(LocalDate.now().plusDays(3));
        when(calculadorMulta.calcular(anyString(), anyLong())).thenReturn(0.0);

        servicio.devolver(5L);

        verify(notificador).notificarDevolucion(usuario);
        verify(notificador, never()).notificarMulta(any(), anyDouble());
    }

    @Test
    void devolucionConRetrasoNotificaDevolucionYMulta() {
        prestamoConVencimiento(LocalDate.now().minusDays(3));
        when(calculadorMulta.calcular(anyString(), anyLong())).thenReturn(3.0);

        servicio.devolver(5L);

        verify(notificador).notificarDevolucion(usuario);
        verify(notificador).notificarMulta(usuario, 3.0);
    }

    @Test
    void devolucionSeCompletaSiElUsuarioYaNoExiste() {
        prestamoConVencimiento(LocalDate.now().plusDays(3));
        when(calculadorMulta.calcular(anyString(), anyLong())).thenReturn(0.0);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        servicio.devolver(5L);

        verify(notificador, never()).notificarDevolucion(any());
        verify(notificador, never()).notificarMulta(any(), anyDouble());
    }
}