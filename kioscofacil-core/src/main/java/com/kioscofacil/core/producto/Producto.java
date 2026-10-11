/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;
import com.kioscofacil.core.compartido.Dinero;

/**
 *
 * @author odlanierbermudez
 */
public record Producto(CodigoBarras codigo, String nombre, Dinero precio) {

    public Producto {
        if (codigo == null) {
            throw new IllegalArgumentException("El producto necesita un código de barras");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El producto necesita un nombre");
        }
        if (precio == null) {
            throw new IllegalArgumentException("El producto necesita un precio");
        }
        nombre = nombre.trim();
    }

    public Producto conPrecio(Dinero nuevoPrecio) {
        return new Producto(codigo, nombre, nuevoPrecio);
    }
}