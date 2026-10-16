
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
 
package com.kioscofacil.app;
 
import com.kioscofacil.ui.AplicacionKiosco;
import com.kioscofacil.ui.Servicios;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
 
/**
 *
 * @author odlanierbermudez
 */
public final class KioscofacilApp {
 
    private static final System.Logger LOG = System.getLogger(KioscofacilApp.class.getName());
 
    private KioscofacilApp() {
    }
 
    public static void main(String[] args) {
        Path archivo = UbicacionDeDatos.archivoDeBaseDeDatos();
        Path carpetaDeRespaldos = UbicacionDeDatos.carpetaDeRespaldos();
        Clock reloj = Clock.systemDefaultZone();
        try (Kiosco kiosco = Kiosco.abrir(archivo, reloj)) {
            Servicios servicios = new Servicios(
                    kiosco.productos(), kiosco.stock(), kiosco.ventas(), kiosco);
            AplicacionKiosco.iniciar(servicios, carpetaDeRespaldos);
            respaldarAlCerrar(kiosco, carpetaDeRespaldos, reloj);
        }
    }
 
    /**
     * Guarda la copia automática del día. Si falla, solo queda en el registro: la ventana
     * ya se cerró y un problema con el respaldo no tiene que impedir cerrar el programa.
     */
    private static void respaldarAlCerrar(Kiosco kiosco, Path carpeta, Clock reloj) {
        try {
            Path guardado = new RespaldoAutomatico(carpeta, reloj, kiosco::copiarBaseEn).ejecutar();
            LOG.log(System.Logger.Level.INFO, "Respaldo automático guardado en " + guardado);
        } catch (IOException | RuntimeException e) {
            LOG.log(System.Logger.Level.WARNING,
                    "No se pudo hacer el respaldo automático en " + carpeta, e);
        }
    }
}