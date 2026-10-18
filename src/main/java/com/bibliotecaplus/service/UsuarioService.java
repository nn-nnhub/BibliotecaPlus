package com.bibliotecaplus.service;

import com.bibliotecaplus.exception.RecursoNoEncontradoException;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import com.bibliotecaplus.validation.ValidadorDatos;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final ValidadorDatos validadorDatos;

    public UsuarioService(UsuarioRepository usuarioRepository, PrestamoRepository prestamoRepository, ValidadorDatos validadorDatos) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.validadorDatos = validadorDatos;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con id: " + id));
    }

    public Usuario guardar(Usuario usuario) {
        validarUsuario(usuario);
        validarDniDisponible(usuario.getDni());
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datosUsuario) {
        validarUsuario(datosUsuario);
        if (usuarioRepository.existsByDniAndIdNot(datosUsuario.getDni(), id)) {
            throw new IllegalArgumentException("El DNI ya está registrado en otro usuario");
        }
        Usuario usuario = buscarPorId(id);
        usuario.setNombre(datosUsuario.getNombre());
        usuario.setApellidos(datosUsuario.getApellidos());
        usuario.setDni(datosUsuario.getDni());
        usuario.setCorreo(datosUsuario.getCorreo());
        usuario.setTelefono(datosUsuario.getTelefono());
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        if (prestamoRepository.existsByUsuarioId(id)) {
            throw new IllegalArgumentException("No se puede eliminar el usuario porque tiene préstamos asociados");
        }
        usuarioRepository.delete(usuario);
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null || validadorDatos.estaVacio(usuario.getNombre())
                || validadorDatos.estaVacio(usuario.getApellidos())
                || validadorDatos.estaVacio(usuario.getDni())
                || validadorDatos.estaVacio(usuario.getCorreo())
                || validadorDatos.estaVacio(usuario.getTelefono())) {
            throw new IllegalArgumentException("Nombres, apellidos, DNI, correo y teléfono son obligatorios");
        }
        if (!validadorDatos.esDniValido(usuario.getDni())) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos");
        }
        if (!validadorDatos.esCorreoValido(usuario.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico no es válido");
        }
    }

    private void validarDniDisponible(String dni) {
        if (usuarioRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("El DNI ya está registrado en otro usuario");
        }
    }
}
