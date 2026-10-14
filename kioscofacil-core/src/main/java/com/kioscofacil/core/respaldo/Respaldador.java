/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.kioscofacil.core.respaldo;

import java.nio.file.Path;

/**
 *
 * @author odlanierbermudez
 */

public interface Respaldador {

    /**
     * Guarda una copia de todos los datos del kiosco en la carpeta indicada
     * (creándola si no existe) y devuelve el archivo creado.
     */
    Path respaldar(Path carpetaDestino);
}