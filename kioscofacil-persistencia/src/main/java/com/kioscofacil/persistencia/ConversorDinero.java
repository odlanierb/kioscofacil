/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import com.kioscofacil.core.compartido.Dinero;
import java.math.BigDecimal;

/**
 *
 * @author odlanierbermudez
 */
final class ConversorDinero {

    private ConversorDinero() {
    }

    static long aCentavos(Dinero dinero) {
        return dinero.monto().movePointRight(2).longValueExact();
    }

    static Dinero deCentavos(long centavos) {
        return new Dinero(BigDecimal.valueOf(centavos, 2));
    }
}
