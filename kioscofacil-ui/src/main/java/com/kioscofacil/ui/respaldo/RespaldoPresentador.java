/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.respaldo;

import com.kioscofacil.core.respaldo.Respaldador;
import com.kioscofacil.ui.compartido.Resultado;
import java.nio.file.Path;
import java.util.Objects;
 

/**
 *
 * @author odlanierbermudez
 */
/**
 * Lógica de la pantalla de respaldo: pide la copia al {@link Respaldador} del core y
 * arma el mensaje que se le muestra a la persona. No conoce JavaFX.
 */
public final class RespaldoPresentador {
 
    private static final System.Logger LOG =
            System.getLogger(RespaldoPresentador.class.getName());
 
    private final Respaldador respaldador;
    private final Path carpetaHabitual;
 
    public RespaldoPresentador(Respaldador respaldador, Path carpetaHabitual) {
        this.respaldador = Objects.requireNonNull(respaldador);
        this.carpetaHabitual = Objects.requireNonNull(carpetaHabitual);
    }
 
    /** La carpeta donde se guardan los respaldos si la persona no elige otra. */
    public Path carpetaHabitual() {
        return carpetaHabitual;
    }
 
    public Resultado respaldarEnCarpetaHabitual() {
        return respaldarEn(carpetaHabitual);
    }
 
    /** Guarda una copia de los datos en la carpeta indicada y dice dónde quedó. */
    public Resultado respaldarEn(Path carpeta) {
        Objects.requireNonNull(carpeta);
        try {
            Path archivo = respaldador.respaldar(carpeta);
            return Resultado.ok("Respaldo guardado en " + archivo.toAbsolutePath() + ".");
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "No se pudo crear el respaldo en " + carpeta, e);
            return Resultado.error("No se pudo guardar el respaldo en " + carpeta.toAbsolutePath()
                    + ". Fijate que tengas permiso para escribir ahí y que haya lugar en el disco, "
                    + "o probá con otra carpeta.");
        }
    }
}
 