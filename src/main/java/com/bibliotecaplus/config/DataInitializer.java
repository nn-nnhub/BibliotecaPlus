package com.bibliotecaplus.config;

import com.bibliotecaplus.model.Libro;
import com.bibliotecaplus.model.Usuario;
import com.bibliotecaplus.repository.LibroRepository;
import com.bibliotecaplus.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatosIniciales(LibroRepository libroRepository,
                                            UsuarioRepository usuarioRepository) {
        return args -> {
            libroRepository.save(new Libro(
                    "Clean Code",
                    "Robert C. Martin",
                    "9780132350884"));
            libroRepository.save(new Libro(
                    "Ingeniería de Software",
                    "Ian Sommerville",
                    "9786071503145"));

            usuarioRepository.save(new Usuario(
                    "Ana Torres",
                    "ana.torres@bibliotecaplus.com"));
            usuarioRepository.save(new Usuario(
                    "Luis Mendoza",
                    "luis.mendoza@bibliotecaplus.com"));
        };
    }
}
