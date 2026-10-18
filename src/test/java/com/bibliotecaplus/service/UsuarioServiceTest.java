package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.validation.ValidadorDatos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PrestamoRepository prestamoRepository;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, prestamoRepository, new ValidadorDatos());
    }

    @Test
    void rechazaDniDuplicado() {
        Usuario usuario = new Usuario("Carlos", "Prueba", "70000001", "carlos@pruebas.local", "999555666");
        when(usuarioRepository.existsByDni("70000001")).thenReturn(true);
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> usuarioService.guardar(usuario));
        assertEquals("El DNI ya está registrado en otro usuario", excepcion.getMessage());
    }

    @Test
    void aceptaDniDisponible() {
        Usuario usuario = new Usuario("Carlos", "Prueba", "70000003", "carlos@pruebas.local", "999555666");
        when(usuarioRepository.existsByDni("70000003")).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        assertEquals("70000003", usuarioService.guardar(usuario).getDni());
    }

    private void assertRechazaGuardar(String dni, String correo, String mensaje) {
        Usuario usuario = new Usuario("Carlos", "Prueba", dni, correo, "999555666");
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> usuarioService.guardar(usuario));
        assertEquals(mensaje, excepcion.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaDniQueNoTieneOchoDigitos() {
        String mensaje = "El DNI debe tener exactamente 8 dígitos";
        assertRechazaGuardar("abc", "carlos@pruebas.local", mensaje);
        assertRechazaGuardar("7000000", "carlos@pruebas.local", mensaje);
        assertRechazaGuardar("700000012", "carlos@pruebas.local", mensaje);
        assertRechazaGuardar("7000000A", "carlos@pruebas.local", mensaje);
        assertRechazaGuardar(" 7000001", "carlos@pruebas.local", mensaje);
    }

    @Test
    void rechazaCorreoSinFormatoValido() {
        String mensaje = "El correo electrónico no es válido";
        assertRechazaGuardar("70000004", "no-es-correo", mensaje);
        assertRechazaGuardar("70000004", "@pruebas.local", mensaje);
        assertRechazaGuardar("70000004", "carlos@", mensaje);
        assertRechazaGuardar("70000004", "carlos@pruebas", mensaje);
        assertRechazaGuardar("70000004", "car los@pruebas.local", mensaje);
    }

    @Test
    void actualizarAplicaLasMismasValidaciones() {
        lenient().when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(new Usuario("Ana", "Torres", "70000001", "ana@pruebas.local", "999111222")));
        Usuario cambios = new Usuario("Carlos", "Prueba", "123", "carlos@pruebas.local", "999555666");
        IllegalArgumentException dniMalo = assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizar(1L, cambios));
        assertEquals("El DNI debe tener exactamente 8 dígitos", dniMalo.getMessage());
        Usuario correoMalo = new Usuario("Carlos", "Prueba", "70000004", "invalido", "999555666");
        IllegalArgumentException mensaje = assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizar(1L, correoMalo));
        assertEquals("El correo electrónico no es válido", mensaje.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaEliminarUsuarioConPrestamosAsociados() {
        Usuario existente = new Usuario("Ana", "Torres", "70000009", "ana@pruebas.local", "999111222");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(prestamoRepository.existsByUsuarioId(1L)).thenReturn(true);
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> usuarioService.eliminar(1L));
        assertEquals("No se puede eliminar el usuario porque tiene préstamos asociados", excepcion.getMessage());
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void eliminaUsuarioSinPrestamos() {
        Usuario existente = new Usuario("Ana", "Torres", "70000009", "ana@pruebas.local", "999111222");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        usuarioService.eliminar(1L);
        verify(usuarioRepository).delete(existente);
    }

    @Test
    void eliminarUsuarioInexistenteLanzaNoEncontrado() {
        assertThrows(com.bibliotecaplus.exception.RecursoNoEncontradoException.class, () -> usuarioService.eliminar(9999L));
        verify(usuarioRepository, never()).delete(any());
    }
}
