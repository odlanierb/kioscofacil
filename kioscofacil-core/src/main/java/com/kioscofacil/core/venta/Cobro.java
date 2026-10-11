/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import com.kioscofacil.core.compartido.Dinero;

/**
 *
 * @author odlanierbermudez
 */
public record Cobro(MedioPago medio, Dinero recibido, Dinero vuelto) {

    public Cobro {
        if (medio == null || recibido == null || vuelto == null) {
            throw new IllegalArgumentException("El cobro tiene datos incompletos");
        }
    }
}
