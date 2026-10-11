/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
public class ProductoTest {

    private final CodigoBarras codigo = new CodigoBarras("7790001000011");

    @Test
    void creaUnProductoValidoYLimpiaElNombre() {
        Producto producto = new Producto(codigo, "  Alfajor  ", Dinero.de("1200"));
        assertEquals("Alfajor", producto.nombre());
    }

    @Test
    void noPermiteNombreVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> new Producto(codigo, "  ", Dinero.de("1200")));
    }

    @Test
    void conPrecioDevuelveUnProductoNuevoSinTocarElOriginal() {
        Producto original = new Producto(codigo, "Alfajor", Dinero.de("1200"));
        Producto conAumento = original.conPrecio(Dinero.de("1500"));

        assertEquals(Dinero.de("1200"), original.precio());
        assertEquals(Dinero.de("1500"), conAumento.precio());
    }
}
