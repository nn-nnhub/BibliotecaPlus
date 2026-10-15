package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con id: " + id));
    }

    public Usuario guardar(Usuario usuario) {
        validarUsuario(usuario);
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datosUsuario) {
        validarUsuario(datosUsuario);
        Usuario usuario = buscarPorId(id);
        usuario.setNombre(datosUsuario.getNombre());
        usuario.setCorreo(datosUsuario.getCorreo());
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El nombre y el correo son obligatorios");
        }
    }
}
