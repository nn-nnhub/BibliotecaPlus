package com.bibliotecaplus.integration;

import com.bibliotecaplus.BibliotecaPlusApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BibliotecaPlusApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:validacion-usuarios-http;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
class ValidacionUsuariosHttpTest {

    @Autowired private MockMvc mvc;

    private String json(String dni, String correo) {
        return "{\"nombre\":\"Carlos\",\"apellidos\":\"Prueba\",\"dni\":\"" + dni + "\",\"correo\":\"" + correo
                + "\",\"telefono\":\"999555666\"}";
    }

    @Test
    void rechazaRegistrarUnUsuarioConDniDeFormatoInvalido() throws Exception {
        for (String dni : new String[] {"abc", "7000000", "700000099", "7000000A"}) {
            mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                            .content(json(dni, "carlos@pruebas.local")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El DNI debe tener exactamente 8 dígitos"));
        }
    }

    @Test
    void rechazaRegistrarUnUsuarioConCorreoInvalido() throws Exception {
        for (String correo : new String[] {"no-es-correo", "carlos@", "carlos@pruebas"}) {
            mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                            .content(json("70000010", correo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El correo electrónico no es válido"));
        }
    }

    @Test
    void registraUnUsuarioConDatosValidos() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content(json("70000011", "carlos.valido@pruebas.local")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dni").value("70000011"));
    }

    @Test
    void actualizarAplicaLasMismasValidacionesYConservaElUsuario() throws Exception {
        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON)
                        .content(json("123", "ana.torres@bibliotecaplus.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El DNI debe tener exactamente 8 dígitos"));
        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON)
                        .content(json("70000001", "invalido")))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/usuarios/1"))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.dni").value("70000001"));
    }

    @Test
    void actualizarUnUsuarioInexistenteResponde404() throws Exception {
        mvc.perform(put("/api/usuarios/9999").contentType(MediaType.APPLICATION_JSON)
                        .content(json("70000012", "otro@pruebas.local")))
                .andExpect(status().isNotFound());
    }
}
