/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
 
import com.kioscofacil.core.compartido.Dinero;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class FormatoDineroTest {
 
    private static final String MENSAJE = "No es un monto";
 
    @Test
    void muestraElDineroConFormatoArgentino() {
        assertEquals("$ 1.500,50", FormatoDinero.mostrar(Dinero.de("1500.5")));
        assertEquals("$ 0,00", FormatoDinero.mostrar(Dinero.CERO));
        assertEquals("$ 1.234.567,00", FormatoDinero.mostrar(Dinero.de("1234567")));
    }
 
    @Test
    void leeEnterosYDecimalesConComaOPunto() {
        assertEquals(Dinero.de("1500"), FormatoDinero.leer("1500", MENSAJE));
        assertEquals(Dinero.de("1500.50"), FormatoDinero.leer("1500,50", MENSAJE));
        assertEquals(Dinero.de("1500.50"), FormatoDinero.leer("1500.50", MENSAJE));
        assertEquals(Dinero.de("800"), FormatoDinero.leer(" $ 800 ", MENSAJE));
    }
 
    @Test
    void rechazaLoQueNoEsUnMontoClaro() {
        for (String texto : new String[] {null, "", "   ", "abc", "-5", "1.500", "10,999", "1,5,0"}) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> FormatoDinero.leer(texto, MENSAJE), "Debió rechazar: " + texto);
            assertEquals(MENSAJE, error.getMessage());
        }
    }
}
 