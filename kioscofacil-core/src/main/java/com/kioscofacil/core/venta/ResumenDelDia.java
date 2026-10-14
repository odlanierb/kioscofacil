/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import com.kioscofacil.core.compartido.Dinero;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author odlanierbermudez
 */
public record ResumenDelDia(LocalDate dia, int cantidadDeVentas, Dinero total,
        Map<MedioPago, Dinero> totalPorMedio) {

    public ResumenDelDia {
        Objects.requireNonNull(dia);
        Objects.requireNonNull(total);
        Objects.requireNonNull(totalPorMedio);
        if (cantidadDeVentas < 0) {
            throw new IllegalArgumentException("La cantidad de ventas no puede ser negativa");
        }
        EnumMap<MedioPago, Dinero> copia = new EnumMap<>(MedioPago.class);
        copia.putAll(totalPorMedio);
        Dinero suma = Dinero.CERO;
        for (Dinero monto : copia.values()) {
            suma = suma.sumar(monto);
        }
        if (!suma.equals(total)) {
            throw new IllegalArgumentException(
                    "El total no coincide con la suma de los totales por medio de pago");
        }
        totalPorMedio = Collections.unmodifiableMap(copia);
    }

    /**
     * Arma el resumen de un día a partir de las ventas registradas ese día.
     */
    public static ResumenDelDia de(LocalDate dia, List<VentaRealizada> ventas) {
        Objects.requireNonNull(dia);
        Objects.requireNonNull(ventas);
        Map<MedioPago, Dinero> porMedio = new EnumMap<>(MedioPago.class);
        for (MedioPago medio : MedioPago.values()) {
            porMedio.put(medio, Dinero.CERO);
        }
        Dinero total = Dinero.CERO;
        for (VentaRealizada venta : ventas) {
            Dinero monto = venta.total();
            total = total.sumar(monto);
            porMedio.merge(venta.cobro().medio(), monto, Dinero::sumar);
        }
        return new ResumenDelDia(dia, ventas.size(), total, porMedio);
    }
}
