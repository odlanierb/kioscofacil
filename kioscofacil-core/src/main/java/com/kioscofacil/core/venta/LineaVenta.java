/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;

/**
 *
 * @author odlanierbermudez
 */
public record LineaVenta(CodigoBarras codigo, String nombre, Dinero precioUnitario, int cantidad) {

    public LineaVenta {
        if (codigo == null || nombre == null || precioUnitario == null) {
            throw new IllegalArgumentException("La línea de venta tiene datos incompletos");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    public static LineaVenta de(Producto producto, int cantidad) {
        return new LineaVenta(producto.codigo(), producto.nombre(), producto.precio(), cantidad);
    }

    public LineaVenta conCantidad(int nuevaCantidad) {
        return new LineaVenta(codigo, nombre, precioUnitario, nuevaCantidad);
    }

    public Dinero subtotal() {
        return precioUnitario.multiplicar(cantidad);
    }
}
