package com.bibliotecaplus.notification;

import com.bibliotecaplus.model.Usuario;

public interface Notificador {
    void notificarRegistro(Usuario usuario);

    void notificarDevolucion(Usuario usuario);

    void notificarMulta(Usuario usuario, double multa);
}