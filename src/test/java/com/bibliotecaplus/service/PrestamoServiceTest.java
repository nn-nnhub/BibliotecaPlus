package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.exception.ConflictoBloqueoLibroException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.notification.Notificador;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.validation.ValidadorDatos;
import com.bibliotecaplus.validation.ValidadorPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {
    @Mock private PrestamoRepository prestamoRepository;
    @Mock private LibroRepository libroRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private Notificador notificador;

    private PrestamoService servicio;
    private Libro libro;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ValidadorDatos validadorDatos = new ValidadorDatos();
        CalculadorMulta calculador = new CalculadorMulta(List.of(new TarifaGeneral(), new TarifaDomicilio(), new TarifaEspecial()));
        servicio = new PrestamoService(prestamoRepository, libroRepository, usuarioRepository, calculador,
                new ValidadorPrestamo(validadorDatos), notificador);
        libro = new Libro("Clean Code", "Robert C. Martin", "9780132350884");
        usuario = new Usuario("Ana", "Torres", "70000001", "ana@biblioteca.local", "999111222");
    }

    private void prepararRegistro() {
        when(libroRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(libro));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    }

    private Prestamo prestamoVencidoHaceDias(String tipo, int dias) {
        Prestamo prestamo = new Prestamo(1L, 1L, LocalDate.now().minusDays(dias + 14L), LocalDate.now().minusDays(dias));
        prestamo.setTipoPrestamo(tipo);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));
        return prestamo;
    }

    @Test
    void soloErroresAlAdquirirBloqueoSeConviertenEnConflicto() {
        for (RuntimeException causa : List.of(new PessimisticLockingFailureException("bloqueo"),
                new QueryTimeoutException("timeout"))) {
            doThrow(causa).when(libroRepository).buscarPorIdConBloqueo(1L);
            var conflicto = assertThrows(ConflictoBloqueoLibroException.class,
                    () -> servicio.registrar(1L, 1L, "DOMICILIO", LocalDate.now().plusDays(7)));
            assertSame(causa, conflicto.getCause());
        }
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void timeoutDeOtraConsultaConservaLaExcepcionOriginal() {
        when(libroRepository.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(libro));
        var causa = new QueryTimeoutException("consulta de usuario");
        when(usuarioRepository.findById(1L)).thenThrow(causa);
        assertSame(causa, assertThrows(QueryTimeoutException.class,
                () -> servicio.registrar(1L, 1L, "DOMICILIO", LocalDate.now().plusDays(7))));
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void registraElTipoNormalizadoEnMayusculas() {
        prepararRegistro();
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));
        Prestamo prestamo = servicio.registrar(1L, 1L, " especial ", LocalDate.now().plusDays(7));
        assertEquals("ESPECIAL", prestamo.getTipoPrestamo());
    }

    @Test
    void usaDomicilioCuandoNoSeIndicaTipo() {
        prepararRegistro();
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("DOMICILIO", servicio.registrar(1L, 1L, null, LocalDate.now().plusDays(7)).getTipoPrestamo());
    }

    @Test
    void rechazaVencimientoPasadoSinOcuparElLibro() {
        prepararRegistro();
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(1L, 1L, "DOMICILIO", LocalDate.now().minusDays(1)));
        assertEquals(true, libro.isDisponible());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void devuelveConLaTarifaDelTipoRegistrado() {
        prestamoVencidoHaceDias("ESPECIAL", 10);
        assertEquals(20.0, servicio.devolver(5L).getMulta());
    }

    @Test
    void prestamoHistoricoSinTipoUsaDomicilio() {
        prestamoVencidoHaceDias(null, 5);
        assertEquals(5.0, servicio.devolver(5L).getMulta());
    }

    @Test
    void devolucionADetiempoNoGeneraMulta() {
        Prestamo prestamo = prestamoVencidoHaceDias("ESPECIAL", 0);
        prestamo.setFechaVencimiento(LocalDate.now());
        assertEquals(0.0, servicio.devolver(5L).getMulta());
    }

    @Test
    void rechazaDevolverDosVeces() {
        Prestamo prestamo = new Prestamo(1L, 1L, LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
        prestamo.setDevuelto(true);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        assertThrows(IllegalArgumentException.class, () -> servicio.devolver(5L));
    }
}
