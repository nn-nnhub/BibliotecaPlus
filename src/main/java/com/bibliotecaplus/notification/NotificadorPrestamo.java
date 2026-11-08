package com.bibliotecaplus.notification;

import com.bibliotecaplus.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class NotificadorPrestamo implements Notificador {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificadorPrestamo.class);

    @Override
    public void notificarRegistro(Usuario usuario) {
        LOGGER.info("Préstamo registrado para {}", usuario.getCorreo());
    }

    @Override
    public void notificarDevolucion(Usuario usuario) {
        LOGGER.info("Devolución registrada para {}", usuario.getCorreo());
    }

    @Override
    public void notificarMulta(Usuario usuario, double multa) {
        LOGGER.info("Multa de S/ {} pendiente para {}",
                String.format(Locale.US, "%.2f", multa), usuario.getCorreo());
    }
}