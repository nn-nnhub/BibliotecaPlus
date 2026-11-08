package com.bibliotecaplus.config;

import com.zaxxer.hikari.SQLExceptionOverride;
import java.sql.SQLException;

/** Un timeout de fila H2 no invalida la conexión: JPA aún necesita hacer rollback. */
public class H2LockTimeoutOverride implements SQLExceptionOverride {
    @java.lang.Override
    public Override adjudicate(SQLException excepcion) {
        return excepcion.getErrorCode() == 50200 && "HYT00".equals(excepcion.getSQLState())
                ? Override.DO_NOT_EVICT : Override.CONTINUE_EVICT;
    }
}
