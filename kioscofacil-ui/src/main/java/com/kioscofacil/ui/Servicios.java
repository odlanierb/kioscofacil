/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui;

import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.respaldo.Respaldador;
import com.kioscofacil.core.stock.StockServicio;
import com.kioscofacil.core.venta.VentaServicio;
import java.util.Objects;

/**
 *
 * @author odlanierbermudez
 */
public record Servicios(ProductoServicio productos, StockServicio stock,
        VentaServicio ventas, Respaldador respaldador) {

    public Servicios {
        Objects.requireNonNull(productos);
        Objects.requireNonNull(stock);
        Objects.requireNonNull(ventas);
        Objects.requireNonNull(respaldador);
    }
}