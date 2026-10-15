package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private UsuarioService usuarioService;

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
}
