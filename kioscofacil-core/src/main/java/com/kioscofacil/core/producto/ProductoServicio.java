/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author odlanierbermudez
 */
public class ProductoServicio {

    private final ProductoRepositorio repositorio;

    public ProductoServicio(ProductoRepositorio repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public Producto registrar(CodigoBarras codigo, String nombre, Dinero precio) {
        Producto producto = new Producto(codigo, nombre, precio);
        repositorio.buscarPorCodigo(codigo).ifPresent(existente -> {
            throw new ProductoDuplicadoException(existente);
        });
        repositorio.guardar(producto);
        return producto;
    }

    public Producto reemplazar(CodigoBarras codigo, String nombre, Dinero precio) {
        buscarPorCodigo(codigo); // falla si el producto no existe
        Producto producto = new Producto(codigo, nombre, precio);
        repositorio.guardar(producto);
        return producto;
    }

    public Producto buscarPorCodigo(CodigoBarras codigo) {
        return repositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "No existe un producto con el código " + codigo.valor()));
    }

    public Producto cambiarPrecio(CodigoBarras codigo, Dinero nuevoPrecio) {
        Producto actualizado = buscarPorCodigo(codigo).conPrecio(nuevoPrecio);
        repositorio.guardar(actualizado);
        return actualizado;
    }

    public List<Producto> listar() {
        return repositorio.listarTodos();
    }
}
