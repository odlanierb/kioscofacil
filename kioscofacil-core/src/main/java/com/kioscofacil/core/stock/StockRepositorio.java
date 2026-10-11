/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.stock;

import com.kioscofacil.core.producto.CodigoBarras;

/**
 *
 * @author odlanierbermudez
 */
public interface StockRepositorio {

    /**
     * Cantidad actual; 0 si nunca se cargó stock de ese producto.
     */
    int cantidadDe(CodigoBarras codigo);

    void guardar(CodigoBarras codigo, int cantidad);

}
