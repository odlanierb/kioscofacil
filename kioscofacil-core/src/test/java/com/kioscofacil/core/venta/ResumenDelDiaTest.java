/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class ResumenDelDiaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 10, 13);

    private static VentaRealizada venta(
            MedioPago medio, String precio, int cantidad, String recibido) {
        Dinero precioUnitario = Dinero.de(precio);
        Dinero total = precioUnitario.multiplicar(cantidad);
        Dinero montoRecibido = Dinero.de(recibido);
        Dinero vuelto = montoRecibido.restar(total);
        return new VentaRealizada(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 10, 13, 12, 0, 0),
                List.of(new LineaVenta(
                        new CodigoBarras("7790001"), "Alfajor", precioUnitario, cantidad)),
                new Cobro(medio, montoRecibido, vuelto));
    }

    @Test
    void sinVentasElResumenEstaTodoEnCero() {
        ResumenDelDia resumen = ResumenDelDia.de(DIA, List.of());

        assertEquals(DIA, resumen.dia());
        assertEquals(0, resumen.cantidadDeVentas());
        assertEquals(Dinero.CERO, resumen.total());
        assertEquals(MedioPago.values().length, resumen.totalPorMedio().size());
        for (MedioPago medio : MedioPago.values()) {
            assertEquals(Dinero.CERO, resumen.totalPorMedio().get(medio));
        }
    }

    @Test
    void sumaElTotalYCuentaLasVentas() {
        ResumenDelDia resumen = ResumenDelDia.de(DIA, List.of(
                venta(MedioPago.EFECTIVO, "850", 2, "2000"),
                venta(MedioPago.TARJETA, "100", 3, "300")));

        assertEquals(2, resumen.cantidadDeVentas());
        assertEquals(Dinero.de("2000"), resumen.total());
    }

    @Test
    void separaElTotalPorMedioDePago() {
        ResumenDelDia resumen = ResumenDelDia.de(DIA, List.of(
                venta(MedioPago.EFECTIVO, "850", 2, "2000"),
                venta(MedioPago.EFECTIVO, "100", 1, "100"),
                venta(MedioPago.TARJETA, "100", 3, "300")));

        assertEquals(Dinero.de("1800"), resumen.totalPorMedio().get(MedioPago.EFECTIVO));
        assertEquals(Dinero.de("300"), resumen.totalPorMedio().get(MedioPago.TARJETA));
        assertEquals(Dinero.CERO, resumen.totalPorMedio().get(MedioPago.TRANSFERENCIA));
    }

    @Test
    void elVueltoNoCuentaComoVendido() {
        ResumenDelDia resumen = ResumenDelDia.de(DIA, List.of(
                venta(MedioPago.EFECTIVO, "850", 2, "5000")));

        assertEquals(Dinero.de("1700"), resumen.total());
        assertEquals(Dinero.de("1700"), resumen.totalPorMedio().get(MedioPago.EFECTIVO));
    }

    @Test
    void noPermiteUnTotalQueNoCoincideConLaSumaPorMedio() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResumenDelDia(DIA, 1, Dinero.de("100"), Map.of()));
    }

    @Test
    void elMapaPorMedioNoSePuedeModificar() {
        ResumenDelDia resumen = ResumenDelDia.de(DIA, List.of());

        assertThrows(UnsupportedOperationException.class,
                () -> resumen.totalPorMedio().put(MedioPago.EFECTIVO, Dinero.de("1")));
    }
}
