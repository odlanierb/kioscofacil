/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class LineaVentaTest {

    private final Producto alfajor =
            new Producto(new CodigoBarras("7790001000011"), "Alfajor", Dinero.de("1200"));

    @Test
    void elSubtotalEsPrecioPorCantidad() {
        assertEquals(Dinero.de("3600"), LineaVenta.de(alfajor, 3).subtotal());
    }

    @Test
    void noPermiteCantidadCero() {
        assertThrows(IllegalArgumentException.class, () -> LineaVenta.de(alfajor, 0));
    }

    @Test
    void noPermiteCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () -> LineaVenta.de(alfajor, -2));
    }
}
