/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
public class DineroTest {

    @Test
    void siempreTieneDosDecimales() {
        assertEquals(Dinero.de("10"), Dinero.de("10.00"));
    }

    @Test
    void sumaSinErroresDeRedondeo() {
        Dinero resultado = Dinero.de("0.10").sumar(Dinero.de("0.20"));
        assertEquals(Dinero.de("0.30"), resultado);
    }

    @Test
    void multiplicaPorCantidad() {
        assertEquals(Dinero.de("750.00"), Dinero.de("250").multiplicar(3));
    }

    @Test
    void noPermiteMontoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> Dinero.de("-1"));
    }

    @Test
    void noPermiteMontoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Dinero(null));
    }

    @Test
    void restaCorrectamente() {
        assertEquals(Dinero.de("749.50"), Dinero.de("1000").restar(Dinero.de("250.50")));
    }

    @Test
    void noPermiteRestarMasDeLoQueHay() {
        assertThrows(IllegalArgumentException.class, () -> Dinero.de("1").restar(Dinero.de("2")));
    }

    @Test
    void comparaMontos() {
        assertTrue(Dinero.de("100").esMenorQue(Dinero.de("200")));
        assertFalse(Dinero.de("200").esMenorQue(Dinero.de("100")));
        assertFalse(Dinero.de("100").esMenorQue(Dinero.de("100")));
    }
}
