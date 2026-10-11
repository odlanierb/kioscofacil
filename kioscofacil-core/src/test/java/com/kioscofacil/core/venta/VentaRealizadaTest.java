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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class VentaRealizadaTest {

    private final Producto alfajor
            = new Producto(new CodigoBarras("7790001000011"), "Alfajor", Dinero.de("1200"));
    private final List<LineaVenta> lineas = List.of(LineaVenta.de(alfajor, 2)); // total 2400
    private final LocalDateTime fecha = LocalDateTime.of(2026, 10, 11, 12, 0);

    @Test
    void calculaElTotalDesdeLasLineas() {
        Cobro cobro = new Cobro(MedioPago.EFECTIVO, Dinero.de("5000"), Dinero.de("2600"));
        VentaRealizada venta = new VentaRealizada(UUID.randomUUID(), fecha, lineas, cobro);
        assertEquals(Dinero.de("2400"), venta.total());
    }

    @Test
    void rechazaUnCobroQueNoCoincideConElTotal() {
        Cobro cobroMalo = new Cobro(MedioPago.EFECTIVO, Dinero.de("5000"), Dinero.de("1000"));
        assertThrows(IllegalArgumentException.class,
                () -> new VentaRealizada(UUID.randomUUID(), fecha, lineas, cobroMalo));
    }

    @Test
    void rechazaUnaVentaSinLineas() {
        Cobro cobro = new Cobro(MedioPago.EFECTIVO, Dinero.CERO, Dinero.CERO);
        assertThrows(IllegalArgumentException.class,
                () -> new VentaRealizada(UUID.randomUUID(), fecha, List.of(), cobro));
    }
}
