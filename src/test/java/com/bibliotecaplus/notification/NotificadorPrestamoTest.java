package com.bibliotecaplus.notification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.bibliotecaplus.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificadorPrestamoTest {

    private final NotificadorPrestamo notificador = new NotificadorPrestamo();
    private final Usuario usuario = new Usuario(
            "Ana", "Torres", "70000001", "ana.torres@bibliotecaplus.com", "999111222");
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void iniciarCaptura() {
        logger = (Logger) LoggerFactory.getLogger(NotificadorPrestamo.class);
        logger.setLevel(Level.INFO);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detenerCaptura() {
        logger.detachAppender(appender);
    }

    @Test
    void notificaRegistroConElCorreoDelUsuario() {
        notificador.notificarRegistro(usuario);

        assertEquals(1, appender.list.size());
        assertEquals("Préstamo registrado para ana.torres@bibliotecaplus.com",
                appender.list.get(0).getFormattedMessage());
    }

    @Test
    void notificaDevolucionConElCorreoDelUsuario() {
        notificador.notificarDevolucion(usuario);

        assertEquals(1, appender.list.size());
        assertEquals("Devolución registrada para ana.torres@bibliotecaplus.com",
                appender.list.get(0).getFormattedMessage());
    }

    @Test
    void notificaMultaConMontoYCorreo() {
        notificador.notificarMulta(usuario, 3.0);

        assertEquals(1, appender.list.size());
        assertEquals("Multa de S/ 3.00 pendiente para ana.torres@bibliotecaplus.com",
                appender.list.get(0).getFormattedMessage());
    }
}