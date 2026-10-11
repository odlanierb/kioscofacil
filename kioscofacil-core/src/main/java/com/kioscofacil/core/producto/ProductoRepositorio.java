/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author odlanierbermudez
 */
public interface ProductoRepositorio {

    /**
     * Guarda el producto; si ya existe uno con ese código, lo reemplaza.
     */
    void guardar(Producto producto);

    Optional<Producto> buscarPorCodigo(CodigoBarras codigo);

    List<Producto> listarTodos();
}
