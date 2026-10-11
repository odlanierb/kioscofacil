/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.compartido;

import java.util.function.Supplier;

/**
 *
 * @author odlanierbermudez
 */
public class EjecutorDirecto implements EjecutorTransaccional {

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        return operacion.get();
    }

}
