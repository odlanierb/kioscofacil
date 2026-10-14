/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui;

import java.util.Objects;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author odlanierbermudez
 */
public final class AplicacionKiosco extends Application {

    private static Servicios servicios;

    /** Abre la ventana. No vuelve hasta que el usuario cierra la última ventana. */
    public static void iniciar(Servicios serviciosDelKiosco) {
        servicios = Objects.requireNonNull(serviciosDelKiosco);
        Application.launch(AplicacionKiosco.class);
    }

    @Override
    public void start(Stage escenario) {
        int cantidad = servicios.productos().listar().size();
        Label mensaje = new Label("Productos registrados: " + cantidad);

        escenario.setTitle("KioscoFacil");
        escenario.setScene(new Scene(new StackPane(mensaje), 480, 320));
        escenario.show();
    }
}
