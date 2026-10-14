/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class UbicacionDeDatosTest {

    private static final Path CASA = Path.of("/casa/ana");

    @Test
    void enWindowsUsaLaCarpetaLocalAppData() {
        Path carpeta = UbicacionDeDatos.carpetaDeDatos(
                "Windows 11", "/datos/local", null, CASA);

        assertEquals(Path.of("/datos/local", "KioscoFacil"), carpeta);
    }

    @Test
    void enWindowsSinVariableUsaAppDataLocalDeLaCarpetaPersonal() {
        Path carpeta = UbicacionDeDatos.carpetaDeDatos("Windows 10", null, null, CASA);

        assertEquals(Path.of("/casa/ana", "AppData", "Local", "KioscoFacil"), carpeta);
    }

    @Test
    void enLinuxUsaXdgDataHomeSiEstaDefinida() {
        Path carpeta = UbicacionDeDatos.carpetaDeDatos("Linux", null, "/xdg", CASA);

        assertEquals(Path.of("/xdg", "KioscoFacil"), carpeta);
    }

    @Test
    void enLinuxSinXdgUsaLocalShareDeLaCarpetaPersonal() {
        Path carpeta = UbicacionDeDatos.carpetaDeDatos("Linux", null, null, CASA);

        assertEquals(Path.of("/casa/ana", ".local", "share", "KioscoFacil"), carpeta);
    }

    @Test
    void enMacUsaApplicationSupport() {
        Path carpeta = UbicacionDeDatos.carpetaDeDatos("Mac OS X", null, null, CASA);

        assertEquals(
                Path.of("/casa/ana", "Library", "Application Support", "KioscoFacil"), carpeta);
    }

    @Test
    void unaVariableVaciaSeTrataComoSiNoEstuviera() {
        Path carpeta = UbicacionDeDatos.carpetaDeDatos("Linux", null, "  ", CASA);

        assertEquals(Path.of("/casa/ana", ".local", "share", "KioscoFacil"), carpeta);
    }

    @Test
    void elArchivoDeLaBaseSeLlamaKioscoDb() {
        assertEquals("kiosco.db",
                UbicacionDeDatos.archivoDeBaseDeDatos().getFileName().toString());
    }
    
        @Test
    void losRespaldosVanEnUnaCarpetaVisibleDeLaCarpetaPersonal() {
        assertEquals(Path.of("/casa/ana", "KioscoFacil", "Respaldos"),
                UbicacionDeDatos.carpetaDeRespaldos(CASA));
    }
}