package com.bibliotecaplus.controller;

import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<Prestamo> listarTodos() {
        return prestamoService.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo registrar(@RequestParam Long libroId,
                              @RequestParam Long usuarioId,
                              @RequestParam(defaultValue = "DOMICILIO") String tipoPrestamo,
                              @RequestParam LocalDate fechaVencimiento) {
        return prestamoService.registrar(libroId, usuarioId, tipoPrestamo, fechaVencimiento);
    }

    @PutMapping("/{id}/devolver")
    public Prestamo devolver(@PathVariable Long id) {
        return prestamoService.devolver(id);
    }
}
