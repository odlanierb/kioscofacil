/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui;

import com.kioscofacil.ui.productos.PantallaProductos;
import com.kioscofacil.ui.productos.ProductosPresentador;
import com.kioscofacil.ui.respaldo.PantallaRespaldo;
import com.kioscofacil.ui.respaldo.RespaldoPresentador;
import com.kioscofacil.ui.resumen.PantallaResumen;
import com.kioscofacil.ui.resumen.ResumenPresentador;
import com.kioscofacil.ui.venta.PantallaVenta;
import com.kioscofacil.ui.venta.VentaPresentador;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 *
 * @author odlanierbermudez
 */
public final class AplicacionKiosco extends Application {
 
    private static Servicios servicios;
    private static Path carpetaDeRespaldos;
 
    /**
     * Abre la ventana. No vuelve hasta que el usuario cierra la última ventana.
     * La carpeta de respaldos es donde se guardan las copias si la persona no elige otra.
     */
    public static void iniciar(Servicios serviciosDelKiosco, Path carpetaDeRespaldosHabitual) {
        servicios = Objects.requireNonNull(serviciosDelKiosco);
        carpetaDeRespaldos = Objects.requireNonNull(carpetaDeRespaldosHabitual);
        Application.launch(AplicacionKiosco.class);
    }
 
    @Override
    public void start(Stage escenario) {
        PantallaVenta venta = new PantallaVenta(
                new VentaPresentador(servicios.productos(), servicios.ventas()));
        PantallaProductos productos = new PantallaProductos(
                new ProductosPresentador(servicios.productos(), servicios.stock()));
        PantallaResumen resumen = new PantallaResumen(
                new ResumenPresentador(servicios.ventas(), Clock.systemDefaultZone()));
        PantallaRespaldo respaldo = new PantallaRespaldo(
                new RespaldoPresentador(servicios.respaldador(), carpetaDeRespaldos));
 
        Tab solapaVenta = new Tab("Venta", venta.raiz());
        Tab solapaProductos = new Tab("Productos", productos.raiz());
        Tab solapaResumen = new Tab("Resumen del día", resumen.raiz());
        Tab solapaRespaldo = new Tab("Respaldo", respaldo.raiz());
        for (Tab solapa : new Tab[] {solapaVenta, solapaProductos, solapaResumen, solapaRespaldo}) {
            solapa.setClosable(false);
        }
 
        TabPane solapas = new TabPane(solapaVenta, solapaProductos, solapaResumen, solapaRespaldo);
        solapas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual == solapaVenta) {
                venta.enfocar();
            } else if (actual == solapaProductos) {
                productos.actualizar(); // el stock cambió con las ventas
            } else if (actual == solapaResumen) {
                resumen.actualizar(); // hay ventas nuevas
            }
        });
 
        escenario.setTitle("KioscoFacil");
        escenario.setScene(new Scene(solapas, 1000, 640));
        escenario.setMinWidth(900);
        escenario.setMinHeight(520);
        escenario.show();
    }
}