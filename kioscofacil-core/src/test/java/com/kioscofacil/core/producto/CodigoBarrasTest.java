/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
public class CodigoBarrasTest {

    @Test
    void quitaEspaciosDelPrincipioYDelFinal() {
        assertEquals("7790001000011", new CodigoBarras("  7790001000011 ").valor());
    }

    @Test
    void noPermiteCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () -> new CodigoBarras("   "));
    }

    @Test
    void noPermiteCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new CodigoBarras(null));
    }
}
