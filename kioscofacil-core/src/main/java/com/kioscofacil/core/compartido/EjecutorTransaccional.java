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
public interface EjecutorTransaccional {

    /** Ejecuta la operación completa o no la ejecuta: si falla, se deshace todo. */
    <T> T ejecutar(Supplier<T> operacion);
}
