/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.productos;

import com.kioscofacil.ui.productos.ProductosPresentador.Fila;
import com.kioscofacil.ui.productos.ProductosPresentador.Resultado;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author odlanierbermudez
 */
public final class PantallaProductos {

    private static final String ESTILO_OK = "-fx-text-fill: #1b6e35;";
    private static final String ESTILO_ERROR = "-fx-text-fill: #b3261e;";

    private final ProductosPresentador presentador;

    private final TableView<Fila> tabla = new TableView<>();
    private final TextField codigo = new TextField();
    private final TextField nombre = new TextField();
    private final TextField precio = new TextField();
    private final TextField cantidad = new TextField();
    private final Label mensaje = new Label();
    private final VBox raiz = new VBox(12);

    public PantallaProductos(ProductosPresentador presentador) {
        this.presentador = Objects.requireNonNull(presentador);

        armarTabla();
        mensaje.setWrapText(true);
        mensaje.setMinHeight(24);

        raiz.setPadding(new Insets(16));
        raiz.setStyle("-fx-font-size: 14px;");
        raiz.getChildren().addAll(armarFormulario(), armarBotones(), mensaje, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        recargar();
        Platform.runLater(codigo::requestFocus);
    }

    public Parent raiz() {
        return raiz;
    }

    private GridPane armarFormulario() {
        codigo.setPromptText("Escaneá o escribí el código");
        nombre.setPromptText("Por ejemplo: Alfajor triple");
        precio.setPromptText("1500 o 1500,50");
        cantidad.setPromptText("Unidades");

        // El escáner escribe el código y manda Enter: salta al campo siguiente.
        codigo.setOnAction(e -> nombre.requestFocus());
        nombre.setOnAction(e -> precio.requestFocus());
        precio.setOnAction(e -> cantidad.requestFocus());

        GridPane grilla = new GridPane();
        grilla.setHgap(8);
        grilla.setVgap(8);
        grilla.addRow(0, new Label("Código de barras"), codigo, new Label("Producto"), nombre);
        grilla.addRow(1, new Label("Precio"), precio, new Label("Cantidad"), cantidad);
        GridPane.setHgrow(codigo, Priority.ALWAYS);
        GridPane.setHgrow(nombre, Priority.ALWAYS);
        return grilla;
    }

    private HBox armarBotones() {
        Button registrar = boton("Registrar producto",
                "Crea el producto. Si escribís una cantidad, es el stock inicial.",
                () -> presentador.registrar(codigo.getText(), nombre.getText(),
                        precio.getText(), cantidad.getText()));
        Button cambiarPrecio = boton("Cambiar precio",
                "Cambia el precio del producto con ese código. Las ventas anteriores no cambian.",
                () -> presentador.cambiarPrecio(codigo.getText(), precio.getText()));
        Button sumarStock = boton("Sumar stock",
                "Suma la cantidad al stock actual, por ejemplo cuando llega mercadería.",
                () -> presentador.sumarStock(codigo.getText(), cantidad.getText()));
        Button fijarStock = boton("Fijar stock",
                "Deja el stock en la cantidad escrita, por ejemplo después de contar.",
                () -> presentador.fijarStock(codigo.getText(), cantidad.getText()));
        return new HBox(8, registrar, cambiarPrecio, sumarStock, fijarStock);
    }

    private Button boton(String texto, String ayuda, Supplier<Resultado> accion) {
        Button boton = new Button(texto);
        boton.setTooltip(new Tooltip(ayuda));
        boton.setOnAction(e -> ejecutar(accion));
        return boton;
    }

    private void ejecutar(Supplier<Resultado> accion) {
        Resultado resultado = accion.get();
        mensaje.setText(resultado.mensaje());
        mensaje.setStyle(resultado.exito() ? ESTILO_OK : ESTILO_ERROR);
        if (resultado.exito()) {
            recargar();
            limpiarFormulario();
        }
    }

    private void armarTabla() {
        tabla.getColumns().add(columna("Código", 160, false,
                fila -> fila.producto().codigo().valor()));
        tabla.getColumns().add(columna("Producto", 320, false,
                fila -> fila.producto().nombre()));
        tabla.getColumns().add(columna("Precio", 130, true,
                fila -> ProductosPresentador.formatearPrecio(fila.producto().precio())));
        tabla.getColumns().add(columna("Stock", 90, true,
                fila -> String.valueOf(fila.stock())));
        tabla.setPlaceholder(new Label("Todavía no hay productos. Completá el código, "
                + "el producto y el precio, y tocá «Registrar producto»."));

        // Al elegir una fila, sus datos pasan al formulario para cambiarlos.
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> {
            if (fila != null) {
                codigo.setText(fila.producto().codigo().valor());
                nombre.setText(fila.producto().nombre());
                precio.setText(ProductosPresentador.precioParaEditar(fila.producto().precio()));
                cantidad.clear();
            }
        });
    }

    private static TableColumn<Fila, String> columna(String titulo, double ancho,
            boolean alinearALaDerecha, Function<Fila, String> valor) {
        TableColumn<Fila, String> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new ReadOnlyStringWrapper(valor.apply(celda.getValue())));
        if (alinearALaDerecha) {
            columna.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
        return columna;
    }

    private void recargar() {
        tabla.getItems().setAll(presentador.filas());
    }

    private void limpiarFormulario() {
        codigo.clear();
        nombre.clear();
        precio.clear();
        cantidad.clear();
        tabla.getSelectionModel().clearSelection();
        codigo.requestFocus();
    }
    
    /** Vuelve a leer productos y stock, por ejemplo después de vender. */
    public void actualizar() {
        recargar();
    }
}
