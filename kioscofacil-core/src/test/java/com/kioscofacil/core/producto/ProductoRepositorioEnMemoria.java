/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author odlanierbermudez
 */
class ProductoRepositorioEnMemoria implements ProductoRepositorio {

    private final Map<CodigoBarras, Producto> productos = new LinkedHashMap<>();

    @Override
    public void guardar(Producto producto) {
        productos.put(producto.codigo(), producto);
    }

    @Override
    public Optional<Producto> buscarPorCodigo(CodigoBarras codigo) {
        return Optional.ofNullable(productos.get(codigo));
    }

    @Override
    public List<Producto> listarTodos() {
        return List.copyOf(productos.values());
    }
}
