/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.ProductoRepositorioEnMemoria;
import com.kioscofacil.core.producto.ProductoServicio;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class StockServicioTest {

    private final ProductoServicio productos =
            new ProductoServicio(new ProductoRepositorioEnMemoria());
    private final StockServicio stock =
            new StockServicio(new StockRepositorioEnMemoria(), productos);
    private final CodigoBarras codigo = new CodigoBarras("7790001000011");

    @Test
    void unProductoSinStockCargadoTieneCero() {
        assertEquals(0, stock.cantidadDe(codigo));
    }

    @Test
    void ingresarSumaAlStockActual() {
        productos.registrar(codigo, "Alfajor", Dinero.de("1200"));
        stock.ingresar(codigo, 10);
        stock.ingresar(codigo, 5);
        assertEquals(15, stock.cantidadDe(codigo));
    }

    @Test
    void noPermiteIngresarStockDeUnProductoInexistente() {
        assertThrows(ReglaDeNegocioException.class, () -> stock.ingresar(codigo, 10));
    }

    @Test
    void noPermiteIngresarCantidadCeroONegativa() {
        productos.registrar(codigo, "Alfajor", Dinero.de("1200"));
        assertThrows(IllegalArgumentException.class, () -> stock.ingresar(codigo, 0));
        assertThrows(IllegalArgumentException.class, () -> stock.ingresar(codigo, -3));
    }

    @Test
    void ajustarFijaLaCantidadReal() {
        productos.registrar(codigo, "Alfajor", Dinero.de("1200"));
        stock.ingresar(codigo, 10);
        stock.ajustar(codigo, 7);
        assertEquals(7, stock.cantidadDe(codigo));
    }

    @Test
    void noPermiteAjustarConCantidadNegativa() {
        productos.registrar(codigo, "Alfajor", Dinero.de("1200"));
        assertThrows(IllegalArgumentException.class, () -> stock.ajustar(codigo, -1));
    }

    @Test
    void descontarRestaYPuedeDejarElStockEnNegativo() {
        productos.registrar(codigo, "Alfajor", Dinero.de("1200"));
        stock.ingresar(codigo, 2);
        stock.descontar(codigo, 5);
        assertEquals(-3, stock.cantidadDe(codigo));
    }
}
