/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

/**
 *
 * @author odlanierbermudez
 */
public record CodigoBarras(String valor) {

    public CodigoBarras {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El código de barras no puede estar vacío");
        }
        valor = valor.trim();
    }
}
