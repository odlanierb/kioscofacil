/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import org.junit.jupiter.api.Test;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;

/**
 *
 * @author odlanierbermudez
 */
class VentaTest {

    private final Producto alfajor
            = new Producto(new CodigoBarras("7790001000011"), "Alfajor", Dinero.de("1200"));
    private final Producto gaseosa
            = new Producto(new CodigoBarras("7790002000022"), "Gaseosa", Dinero.de("2500"));

    @Test
    void unaVentaNuevaEstaVaciaYSuTotalEsCero() {
        Venta venta = new Venta();
        assertTrue(venta.estaVacia());
        assertEquals(Dinero.CERO, venta.total());
    }

    @Test
    void elTotalSumaTodasLasLineas() {
        Venta venta = new Venta();
        venta.agregar(alfajor, 2);
        venta.agregar(gaseosa, 1);
        assertEquals(Dinero.de("4900"), venta.total());
    }

    @Test
    void agregarElMismoProductoSumaLaCantidadEnLaMismaLinea() {
        Venta venta = new Venta();
        venta.agregar(alfajor, 1);
        venta.agregar(alfajor, 2);

        assertEquals(1, venta.lineas().size());
        assertEquals(3, venta.lineas().get(0).cantidad());
    }

    @Test
    void noPermiteAgregarCantidadCeroONegativa() {
        Venta venta = new Venta();
        assertThrows(IllegalArgumentException.class, () -> venta.agregar(alfajor, 0));
        assertThrows(IllegalArgumentException.class, () -> venta.agregar(alfajor, -1));
    }

    @Test
    void cobraEnEfectivoYCalculaElVuelto() {
        Venta venta = new Venta();
        venta.agregar(alfajor, 2); // 2400

        Cobro cobro = venta.cobrar(MedioPago.EFECTIVO, Dinero.de("5000"));

        assertEquals(Dinero.de("2600"), cobro.vuelto());
        assertTrue(venta.estaCobrada());
    }

    @Test
    void noCobraSiElMontoRecibidoNoAlcanza() {
        Venta venta = new Venta();
        venta.agregar(alfajor, 2);
        assertThrows(ReglaDeNegocioException.class,
                () -> venta.cobrar(MedioPago.EFECTIVO, Dinero.de("2000")));
    }

    @Test
    void noCobraUnaVentaVacia() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new Venta().cobrar(MedioPago.EFECTIVO, Dinero.de("1000")));
    }

    @Test
    void cobraConTransferenciaPorElMontoExacto() {
        Venta venta = new Venta();
        venta.agregar(gaseosa, 1);
        Cobro cobro = venta.cobrar(MedioPago.TRANSFERENCIA, Dinero.de("2500"));
        assertEquals(Dinero.CERO, cobro.vuelto());
    }

    @Test
    void soloElEfectivoPermiteDarVuelto() {
        Venta venta = new Venta();
        venta.agregar(gaseosa, 1);
        assertThrows(ReglaDeNegocioException.class,
                () -> venta.cobrar(MedioPago.TRANSFERENCIA, Dinero.de("3000")));
    }

    @Test
    void unaVentaCobradaNoSePuedeModificarNiCobrarDeNuevo() {
        Venta venta = new Venta();
        venta.agregar(alfajor, 1);
        venta.cobrar(MedioPago.EFECTIVO, Dinero.de("1200"));

        assertThrows(ReglaDeNegocioException.class, () -> venta.agregar(gaseosa, 1));
        assertThrows(ReglaDeNegocioException.class,
                () -> venta.cobrar(MedioPago.EFECTIVO, Dinero.de("1200")));
    }
}
