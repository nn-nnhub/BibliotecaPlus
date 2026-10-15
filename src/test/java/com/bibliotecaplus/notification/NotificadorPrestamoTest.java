package com.bibliotecaplus.notification;

import com.bibliotecaplus.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificadorPrestamoTest {
    @Test
    void notificaRegistroSinDependerDelServicioDePrestamo() {
        Notificador notificador = new NotificadorPrestamo();
        Usuario usuario = new Usuario("Ana", "Torres", "70000001", "ana@biblioteca.local", "999111222");
        assertDoesNotThrow(() -> notificador.notificarRegistro(usuario));
    }
}
