/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class ConversorDineroTest {

    @Test
    void convierteDineroACentavos() {
        assertEquals(1050L, ConversorDinero.aCentavos(Dinero.de("10.50")));
    }

    @Test
    void convierteCeroACeroCentavos() {
        assertEquals(0L, ConversorDinero.aCentavos(Dinero.CERO));
    }

    @Test
    void convierteCentavosADinero() {
        assertEquals(Dinero.de("10.50"), ConversorDinero.deCentavos(1050L));
    }

    @Test
    void convierteCentavosMenoresQueUnPesoADinero() {
        assertEquals(Dinero.de("0.05"), ConversorDinero.deCentavos(5L));
    }

    @Test
    void idaYVueltaConservaElValor() {
        String[] montos = {"0.00", "0.01", "0.99", "1", "10.50", "1234567.89"};
        for (String monto : montos) {
            Dinero original = Dinero.de(monto);
            Dinero devuelto = ConversorDinero.deCentavos(ConversorDinero.aCentavos(original));
            assertEquals(original, devuelto);
        }
    }

    @Test
    void rechazaCentavosNegativos() {
        assertThrows(IllegalArgumentException.class, () -> ConversorDinero.deCentavos(-1L));
    }
}