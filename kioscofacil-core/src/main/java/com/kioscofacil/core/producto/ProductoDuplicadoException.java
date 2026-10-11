/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

import com.kioscofacil.core.compartido.ReglaDeNegocioException;

/**
 *
 * @author odlanierbermudez
 */
public class ProductoDuplicadoException extends ReglaDeNegocioException {

    private final Producto existente;

    public ProductoDuplicadoException(Producto existente) {
        super("Ya existe un producto con el código " + existente.codigo().valor());
        this.existente = existente;
    }

    public Producto existente() {
        return existente;
    }
}
