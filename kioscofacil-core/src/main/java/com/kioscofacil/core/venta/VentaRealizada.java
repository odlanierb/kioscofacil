/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import com.kioscofacil.core.compartido.Dinero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author odlanierbermudez
 */
public record VentaRealizada(UUID id, LocalDateTime fecha, List<LineaVenta> lineas, Cobro cobro) {

    public VentaRealizada {
        if (id == null || fecha == null || lineas == null || cobro == null) {
            throw new IllegalArgumentException("La venta realizada tiene datos incompletos");
        }
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("Una venta realizada necesita al menos una línea");
        }
        lineas = List.copyOf(lineas);
        Dinero total = totalDe(lineas);
        if (cobro.recibido().esMenorQue(total)
                || !cobro.recibido().restar(total).equals(cobro.vuelto())) {
            throw new IllegalArgumentException("El cobro no coincide con el total de las líneas");
        }
    }

    public Dinero total() {
        return totalDe(lineas);
    }

    private static Dinero totalDe(List<LineaVenta> lineas) {
        Dinero total = Dinero.CERO;
        for (LineaVenta linea : lineas) {
            total = total.sumar(linea.subtotal());
        }
        return total;
    }
}