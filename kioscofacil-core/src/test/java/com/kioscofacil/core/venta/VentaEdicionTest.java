/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class VentaEdicionTest {

    private static final Producto ALFAJOR =
            new Producto(new CodigoBarras("7790001"), "Alfajor", Dinero.de("850"));
    private static final Producto CHICLE =
            new Producto(new CodigoBarras("7790002"), "Chicle", Dinero.de("100"));
    private static final Producto GASEOSA =
            new Producto(new CodigoBarras("7790003"), "Gaseosa", Dinero.de("2000"));

    @Test
    void quitarSacaLaLineaCompletaYActualizaElTotal() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 2);
        venta.agregar(CHICLE, 1);

        venta.quitar(ALFAJOR.codigo());

        assertEquals(1, venta.lineas().size());
        assertEquals(Dinero.de("100"), venta.total());
    }

    @Test
    void quitarConservaElOrdenDeLasOtrasLineas() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 1);
        venta.agregar(CHICLE, 1);
        venta.agregar(GASEOSA, 1);

        venta.quitar(CHICLE.codigo());

        List<CodigoBarras> codigos = venta.lineas().stream().map(LineaVenta::codigo).toList();
        assertEquals(List.of(ALFAJOR.codigo(), GASEOSA.codigo()), codigos);
    }

    @Test
    void quitarLaUltimaLineaDejaLaVentaVacia() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 1);

        venta.quitar(ALFAJOR.codigo());

        assertTrue(venta.estaVacia());
        assertEquals(Dinero.CERO, venta.total());
    }

    @Test
    void quitarUnProductoQueNoEstaEnLaVentaFalla() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 1);

        assertThrows(ReglaDeNegocioException.class, () -> venta.quitar(CHICLE.codigo()));
    }

    @Test
    void noSePuedeQuitarUnaLineaDeUnaVentaCobrada() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 1);
        venta.cobrar(MedioPago.EFECTIVO, Dinero.de("850"));

        assertThrows(ReglaDeNegocioException.class, () -> venta.quitar(ALFAJOR.codigo()));

        assertEquals(1, venta.lineas().size());
    }

    @Test
    void cambiarCantidadReemplazaLaCantidadDeLaLinea() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 3);

        venta.cambiarCantidad(ALFAJOR.codigo(), 1);

        assertEquals(1, venta.lineas().get(0).cantidad());
        assertEquals(Dinero.de("850"), venta.total());
    }

    @Test
    void cambiarCantidadACeroFallaYNoModificaLaLinea() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 3);

        assertThrows(IllegalArgumentException.class,
                () -> venta.cambiarCantidad(ALFAJOR.codigo(), 0));

        assertEquals(3, venta.lineas().get(0).cantidad());
    }

    @Test
    void cambiarLaCantidadDeUnProductoQueNoEstaEnLaVentaFalla() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 1);

        assertThrows(ReglaDeNegocioException.class,
                () -> venta.cambiarCantidad(CHICLE.codigo(), 2));
    }

    @Test
    void noSePuedeCambiarLaCantidadEnUnaVentaCobrada() {
        Venta venta = new Venta();
        venta.agregar(ALFAJOR, 1);
        venta.cobrar(MedioPago.EFECTIVO, Dinero.de("850"));

        assertThrows(ReglaDeNegocioException.class,
                () -> venta.cambiarCantidad(ALFAJOR.codigo(), 2));

        assertEquals(1, venta.lineas().get(0).cantidad());
    }
}