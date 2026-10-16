/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.respaldo;

import com.kioscofacil.ui.compartido.Resultado;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Pantalla para guardar una copia de todos los datos del kiosco, en la carpeta habitual
 * o en otra que elija la persona (por ejemplo, un pendrive).
 */
public final class PantallaRespaldo {
 
    private static final String ESTILO_OK = "-fx-text-fill: #1b6e35;";
    private static final String ESTILO_ERROR = "-fx-text-fill: #b3261e;";
 
    private final RespaldoPresentador presentador;
    private final Label mensaje = new Label();
    private final VBox raiz = new VBox(14);
 
    public PantallaRespaldo(RespaldoPresentador presentador) {
        this.presentador = Objects.requireNonNull(presentador);
 
        Label titulo = new Label("Respaldo de datos");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
 
        Label explicacion = new Label("El respaldo es una copia de todos tus productos, stock y "
                + "ventas. Guardá también una copia en un pendrive o en la nube: así no perdés "
                + "nada si se rompe o te roban la computadora.");
        explicacion.setWrapText(true);
 
        Label automatico = new Label("Además, al cerrar el programa se guarda solo una copia "
                + "automática en la carpeta habitual: una por día, con el nombre "
                + "kiosco-auto-AAAA-MM-DD.db. Las más viejas se borran solas para no llenar el disco.");
        automatico.setWrapText(true);
 
        Label carpeta = new Label("Carpeta habitual: "
                + presentador.carpetaHabitual().toAbsolutePath());
        carpeta.setWrapText(true);
 
        Button ahora = new Button("Hacer respaldo ahora");
        ahora.setOnAction(e -> mostrar(presentador.respaldarEnCarpetaHabitual()));
        Button otraCarpeta = new Button("Guardar en otra carpeta…");
        otraCarpeta.setOnAction(e -> elegirCarpeta());
 
        mensaje.setWrapText(true);
        mensaje.setMinHeight(24);
 
        raiz.setPadding(new Insets(16));
        raiz.setStyle("-fx-font-size: 14px;");
        raiz.getChildren().addAll(titulo, explicacion, automatico, carpeta,
                new HBox(8, ahora, otraCarpeta), mensaje);
    }
 
    public Parent raiz() {
        return raiz;
    }
 
    private void elegirCarpeta() {
        DirectoryChooser selector = new DirectoryChooser();
        selector.setTitle("Elegí dónde guardar el respaldo");
        Path habitual = presentador.carpetaHabitual();
        if (Files.isDirectory(habitual)) {
            selector.setInitialDirectory(habitual.toFile());
        }
        Window ventana = raiz.getScene() == null ? null : raiz.getScene().getWindow();
        File elegida = selector.showDialog(ventana);
        if (elegida != null) {
            mostrar(presentador.respaldarEn(elegida.toPath()));
        }
    }
 
    private void mostrar(Resultado resultado) {
        mensaje.setText(resultado.mensaje());
        mensaje.setStyle(resultado.exito() ? ESTILO_OK : ESTILO_ERROR);
    }
}
 