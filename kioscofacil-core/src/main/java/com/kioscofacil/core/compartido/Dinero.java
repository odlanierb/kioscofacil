/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.kioscofacil.core.compartido;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author odlanierbermudez
 */
public record Dinero(BigDecimal monto) {

    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

    public Dinero {
        if (monto == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }
        if (monto.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinero de(String valor) {
        return new Dinero(new BigDecimal(valor));
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(monto.add(otro.monto));
    }

    public Dinero multiplicar(int cantidad) {
        return new Dinero(monto.multiply(BigDecimal.valueOf(cantidad)));
    }

    public Dinero restar(Dinero otro) {
        return new Dinero(monto.subtract(otro.monto));
    }

    public boolean esMenorQue(Dinero otro) {
        return monto.compareTo(otro.monto) < 0;
    }

}
