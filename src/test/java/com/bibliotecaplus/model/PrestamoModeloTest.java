package com.bibliotecaplus.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrestamoModeloTest {

    private final LocalDate prestamo = LocalDate.of(2026, 10, 1);
    private final LocalDate vencimiento = LocalDate.of(2026, 10, 15);

    private Prestamo nuevoPrestamo() {
        return new Prestamo(1L, 2L, prestamo, vencimiento);
    }

    @Test
    void constructorAsignaIdsYFechas() {
        Prestamo p = nuevoPrestamo();

        assertEquals(1L, p.getLibroId());
        assertEquals(2L, p.getUsuarioId());
        assertEquals(prestamo, p.getFechaPrestamo());
        assertEquals(vencimiento, p.getFechaVencimiento());
    }

    @Test
    void prestamoNuevoQuedaSinDevolverYSinMulta() {
        Prestamo p = nuevoPrestamo();

        assertFalse(p.isDevuelto());
        assertEquals(0.0, p.getMulta());
        assertNull(p.getFechaDevolucion());
    }

    @Test
    void sinTipoSeLeeComoDomicilio() {
        assertEquals(Prestamo.TIPO_POR_DEFECTO, nuevoPrestamo().getTipoPrestamo());
        assertEquals("DOMICILIO", nuevoPrestamo().getTipoPrestamo());
    }

    @Test
    void conTipoAsignadoSeDevuelveEseTipo() {
        Prestamo p = nuevoPrestamo();
        p.setTipoPrestamo("ESPECIAL");

        assertEquals("ESPECIAL", p.getTipoPrestamo());
    }

    @Test
    void tipoNuloVuelveAlTipoPorDefecto() {
        Prestamo p = nuevoPrestamo();
        p.setTipoPrestamo("SALA");
        p.setTipoPrestamo(null);

        assertEquals("DOMICILIO", p.getTipoPrestamo());
    }

    @Test
    void settersDeDevolucionYMultaActualizanElEstado() {
        Prestamo p = nuevoPrestamo();
        LocalDate devolucion = LocalDate.of(2026, 10, 18);

        p.setDevuelto(true);
        p.setFechaDevolucion(devolucion);
        p.setMulta(3.0);

        assertTrue(p.isDevuelto());
        assertEquals(devolucion, p.getFechaDevolucion());
        assertEquals(3.0, p.getMulta());
    }

    @Test
    void settersDeIdsYFechasActualizanElPrestamo() {
        Prestamo p = nuevoPrestamo();
        LocalDate nuevoVencimiento = LocalDate.of(2026, 10, 22);

        p.setLibroId(9L);
        p.setUsuarioId(8L);
        p.setFechaPrestamo(LocalDate.of(2026, 10, 2));
        p.setFechaVencimiento(nuevoVencimiento);

        assertEquals(9L, p.getLibroId());
        assertEquals(8L, p.getUsuarioId());
        assertEquals(LocalDate.of(2026, 10, 2), p.getFechaPrestamo());
        assertEquals(nuevoVencimiento, p.getFechaVencimiento());
    }
}