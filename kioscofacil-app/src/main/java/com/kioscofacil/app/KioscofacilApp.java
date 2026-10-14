/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.kioscofacil.app;

import com.kioscofacil.ui.AplicacionKiosco;
import com.kioscofacil.ui.Servicios;
import java.nio.file.Path;
import java.time.Clock;

/**
 *
 * @author odlanierbermudez
 */
public final class KioscofacilApp {

    private KioscofacilApp() {
    }

    public static void main(String[] args) {
        Path archivo = UbicacionDeDatos.archivoDeBaseDeDatos();
        try (Kiosco kiosco = Kiosco.abrir(archivo, Clock.systemDefaultZone())) {
            Servicios servicios = new Servicios(
                    kiosco.productos(), kiosco.stock(), kiosco.ventas(), kiosco);
            AplicacionKiosco.iniciar(servicios);
        }
    }
}