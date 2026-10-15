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
            if (libroRepository.count() > 0 || usuarioRepository.count() > 0) {
                return;
            }
            libroRepository.save(new Libro(
                    "Clean Code",
                    "Robert C. Martin",
                    "9780132350884"));
            libroRepository.save(new Libro(
                    "Ingeniería de Software",
                    "Ian Sommerville",
                    "9786071503145"));

            usuarioRepository.save(new Usuario(
                    "Ana", "Torres", "70000001", "ana.torres@bibliotecaplus.com", "999111222"));
            usuarioRepository.save(new Usuario(
                    "Luis", "Mendoza", "70000002", "luis.mendoza@bibliotecaplus.com", "999333444"));
        };
    }
}
