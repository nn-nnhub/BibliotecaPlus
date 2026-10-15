package com.bibliotecaplus.notification;

import com.bibliotecaplus.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificadorPrestamo implements Notificador {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificadorPrestamo.class);

    @Override
    public void notificarRegistro(Usuario usuario) {
        LOGGER.info("Préstamo registrado para {}", usuario.getCorreo());
    }
}
