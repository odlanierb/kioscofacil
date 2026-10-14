/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.app;

import java.nio.file.Path;
import java.util.Locale;
/**
 *
 * @author odlanierbermudez
 */
final class UbicacionDeDatos {

    static final String NOMBRE_CARPETA = "KioscoFacil";
    static final String NOMBRE_ARCHIVO = "kiosco.db";
    static final String NOMBRE_CARPETA_RESPALDOS = "Respaldos";

    private UbicacionDeDatos() {
    }

    /** Archivo de la base de datos en la PC donde corre el programa. */
    static Path archivoDeBaseDeDatos() {
        return carpetaDeDatos(
                System.getProperty("os.name"),
                System.getenv("LOCALAPPDATA"),
                System.getenv("XDG_DATA_HOME"),
                Path.of(System.getProperty("user.home")))
                .resolve(NOMBRE_ARCHIVO);
    }

    /** Carpeta visible donde se guardan los respaldos por defecto. */
    static Path carpetaDeRespaldos() {
        return carpetaDeRespaldos(Path.of(System.getProperty("user.home")));
    }

    static Path carpetaDeRespaldos(Path carpetaPersonal) {
        return carpetaPersonal.resolve(NOMBRE_CARPETA).resolve(NOMBRE_CARPETA_RESPALDOS);
    }

    static Path carpetaDeDatos(String sistema, String localAppData,
            String xdgDataHome, Path carpetaPersonal) {
        String nombre = sistema == null ? "" : sistema.toLowerCase(Locale.ROOT);
        Path base;
        if (nombre.startsWith("windows")) {
            base = hayValor(localAppData)
                    ? Path.of(localAppData)
                    : carpetaPersonal.resolve("AppData").resolve("Local");
        } else if (nombre.startsWith("mac")) {
            base = carpetaPersonal.resolve("Library").resolve("Application Support");
        } else {
            base = hayValor(xdgDataHome)
                    ? Path.of(xdgDataHome)
                    : carpetaPersonal.resolve(".local").resolve("share");
        }
        return base.resolve(NOMBRE_CARPETA);
    }

    private static boolean hayValor(String texto) {
        return texto != null && !texto.isBlank();
    }
}