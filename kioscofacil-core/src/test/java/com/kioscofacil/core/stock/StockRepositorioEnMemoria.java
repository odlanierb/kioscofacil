/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.stock;

import com.kioscofacil.core.producto.CodigoBarras;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author odlanierbermudez
 */
public class StockRepositorioEnMemoria implements StockRepositorio {

    private final Map<CodigoBarras, Integer> cantidades = new HashMap<>();

    @Override
    public int cantidadDe(CodigoBarras codigo) {
        return cantidades.getOrDefault(codigo, 0);
    }

    @Override
    public void guardar(CodigoBarras codigo, int cantidad) {
        cantidades.put(codigo, cantidad);
    }
}