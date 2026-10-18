package com.bibliotecaplus.service;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Prestamo;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.PrestamoRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReporteService {
    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteService(PrestamoRepository prestamoRepository, LibroRepository libroRepository, UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Map<String, Object>> listarPrestamos() {
        List<Prestamo> prestamos = prestamoRepository.findAll();
        Map<Long, String> titulos = titulosDeLibros(prestamos.stream().map(Prestamo::getLibroId));
        Map<Long, String> nombres = nombresDeUsuarios(prestamos.stream().map(Prestamo::getUsuarioId));
        return prestamos.stream().map(prestamo -> convertirPrestamo(prestamo, titulos, nombres)).toList();
    }

    public Map<String, Object> generarResumen() {
        List<Prestamo> prestamos = prestamoRepository.findAll();
        Map<Long, Long> frecuencia = prestamos.stream().collect(Collectors.groupingBy(Prestamo::getLibroId, Collectors.counting()));
        List<Map<String, Object>> librosMasPrestados = frecuencia.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entrada -> {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("libro", libroRepository.findById(entrada.getKey()).map(libro -> libro.getTitulo()).orElse("Libro " + entrada.getKey()));
                    fila.put("prestamos", entrada.getValue());
                    return fila;
                }).toList();
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalPrestamos", prestamos.size());
        resumen.put("prestamosActivos", prestamos.stream().filter(prestamo -> !prestamo.isDevuelto()).count());
        resumen.put("prestamosDevueltos", prestamos.stream().filter(Prestamo::isDevuelto).count());
        resumen.put("totalMultas", prestamos.stream().mapToDouble(Prestamo::getMulta).sum());
        resumen.put("librosMasPrestados", librosMasPrestados);
        return resumen;
    }

    private Map<Long, String> titulosDeLibros(Stream<Long> libroIds) {
        List<Long> ids = libroIds.filter(Objects::nonNull).distinct().toList();
        return libroRepository.findAllById(ids).stream()
                .filter(libro -> libro.getTitulo() != null)
                .collect(Collectors.toMap(Libro::getId, Libro::getTitulo));
    }

    private Map<Long, String> nombresDeUsuarios(Stream<Long> usuarioIds) {
        List<Long> ids = usuarioIds.filter(Objects::nonNull).distinct().toList();
        return usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, usuario -> (usuario.getNombre() + " " + usuario.getApellidos()).trim()));
    }

    private Map<String, Object> convertirPrestamo(Prestamo prestamo, Map<Long, String> titulos, Map<Long, String> nombres) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("id", prestamo.getId());
        fila.put("libro", titulos.getOrDefault(prestamo.getLibroId(), "Libro " + prestamo.getLibroId()));
        fila.put("usuario", nombres.getOrDefault(prestamo.getUsuarioId(), "Usuario " + prestamo.getUsuarioId()));
        fila.put("fechaPrestamo", prestamo.getFechaPrestamo());
        fila.put("fechaVencimiento", prestamo.getFechaVencimiento());
        fila.put("devuelto", prestamo.isDevuelto() ? "Sí" : "No");
        fila.put("multa", prestamo.getMulta());
        return fila;
    }
}
