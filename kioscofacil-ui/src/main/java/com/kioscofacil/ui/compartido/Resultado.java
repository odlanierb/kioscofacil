/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.compartido;
import java.util.Objects;
/**
 *
 * @author odlanierbermudez
 */
/** Qué pasó al ejecutar una acción de una pantalla y qué mensaje mostrarle a la persona. */
public record Resultado(boolean exito, String mensaje) {
 
    public Resultado {
        Objects.requireNonNull(mensaje);
    }
 
    public static Resultado ok(String mensaje) {
        return new Resultado(true, mensaje);
    }
 
    public static Resultado error(String mensaje) {
        return new Resultado(false, mensaje);
    }
}
 