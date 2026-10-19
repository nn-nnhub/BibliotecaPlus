package com.bibliotecaplus.controller;

import com.bibliotecaplus.dto.FichaLector;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.service.FichaLectorService;
import com.bibliotecaplus.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final FichaLectorService fichaLectorService;

    public UsuarioController(UsuarioService usuarioService, FichaLectorService fichaLectorService) {
        this.usuarioService = usuarioService;
        this.fichaLectorService = fichaLectorService;
    }

    @GetMapping
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @GetMapping("/{id}/ficha")
    public FichaLector obtenerFicha(@PathVariable Long id) {
        return fichaLectorService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario guardar(@RequestBody Usuario usuario) {
        return usuarioService.guardar(usuario);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return usuarioService.actualizar(id, usuario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
