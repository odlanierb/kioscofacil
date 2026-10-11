/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.stock;

import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.ProductoServicio;
import java.util.Objects;

/**
 *
 * @author odlanierbermudez
 */
public class StockServicio {

    private final StockRepositorio repositorio;
    private final ProductoServicio productos;

    public StockServicio(StockRepositorio repositorio, ProductoServicio productos) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.productos = Objects.requireNonNull(productos);
    }

    public int cantidadDe(CodigoBarras codigo) {
        return repositorio.cantidadDe(codigo);
    }

    /** Entra mercadería: suma al stock actual. */
    public void ingresar(CodigoBarras codigo, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a ingresar debe ser mayor a cero");
        }
        productos.buscarPorCodigo(codigo); // falla si el producto no existe
        repositorio.guardar(codigo, repositorio.cantidadDe(codigo) + cantidad);
    }

    /** Corrige el stock con lo que realmente hay (por ejemplo, después de contar). */
    public void ajustar(CodigoBarras codigo, int cantidadReal) {
        if (cantidadReal < 0) {
            throw new IllegalArgumentException("La cantidad real no puede ser negativa");
        }
        productos.buscarPorCodigo(codigo);
        repositorio.guardar(codigo, cantidadReal);
    }

    /** Sale mercadería por una venta. Nunca bloquea: el stock puede quedar en negativo. */
    public void descontar(CodigoBarras codigo, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero");
        }
        repositorio.guardar(codigo, repositorio.cantidadDe(codigo) - cantidad);
    }
}
