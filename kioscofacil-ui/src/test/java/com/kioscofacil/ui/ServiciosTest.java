/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class ServiciosTest {

    @Test
    void noAceptaServiciosNulos() {
        assertThrows(NullPointerException.class, () -> new Servicios(null, null, null, null));
    }
}