/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.venta;

import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.ui.compartido.FormatoDinero;
import com.kioscofacil.ui.compartido.Resultado;
import java.util.Objects;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Pantalla de venta: se escanean los productos, se elige cómo paga el cliente y
 * se cobra. Solo arma los controles y reenvía lo escrito al
 * {@link VentaPresentador}.
 */
public final class PantallaVenta {

    private static final String ESTILO_OK = "-fx-text-fill: #1b6e35;";
    private static final String ESTILO_ERROR = "-fx-text-fill: #b3261e;";

    private final VentaPresentador presentador;

    private final TableView<LineaVenta> tabla = new TableView<>();
    private final TextField escaner = new TextField();
    private final TextField recibido = new TextField();
    private final Label mensaje = new Label();
    private final Label total = new Label();
    private final Label vuelto = new Label();
    private final ToggleGroup medios = new ToggleGroup();
    private final BorderPane raiz = new BorderPane();

    public PantallaVenta(VentaPresentador presentador) {
        this.presentador = Objects.requireNonNull(presentador);

        armarTabla();
        raiz.setPadding(new Insets(16));
        raiz.setStyle("-fx-font-size: 14px;");
        raiz.setTop(armarEncabezado());
        raiz.setCenter(armarLineas());
        raiz.setRight(armarCobro());

        recargar(null);
        enfocar();
    }

    public Parent raiz() {
        return raiz;
    }

    /**
     * Deja el cursor en el campo del escáner, listo para el próximo producto.
     */
    public void enfocar() {
        Platform.runLater(escaner::requestFocus);
    }

    private VBox armarEncabezado() {
        escaner.setPromptText("Escaneá un producto, o escribí el código y apretá Enter");
        escaner.setStyle("-fx-font-size: 18px;");
        escaner.setOnAction(e -> escanear());

        mensaje.setWrapText(true);
        mensaje.setMinHeight(24);

        VBox encabezado = new VBox(8, escaner, mensaje);
        encabezado.setPadding(new Insets(0, 0, 12, 0));
        return encabezado;
    }

    private VBox armarLineas() {
        Button restarUno = new Button("Restar 1");
        restarUno.setOnAction(e -> sobreLaLineaElegida(presentador::restarUno));
        Button sumarUno = new Button("Sumar 1");
        sumarUno.setOnAction(e -> sobreLaLineaElegida(presentador::sumarUno));
        Button quitar = new Button("Quitar producto");
        quitar.setOnAction(e -> sobreLaLineaElegida(presentador::quitar));

        for (Button boton : new Button[]{restarUno, sumarUno, quitar}) {
            boton.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        }

        VBox lineas = new VBox(8, tabla, new HBox(8, restarUno, sumarUno, quitar));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return lineas;
    }

    private VBox armarCobro() {
        total.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        vuelto.setStyle("-fx-font-size: 18px;");
        vuelto.setWrapText(true);
        vuelto.setMinHeight(28);

        VBox opciones = new VBox(6);
        for (MedioPago medio : MedioPago.values()) {
            RadioButton opcion = new RadioButton(VentaPresentador.nombreDe(medio));
            opcion.setUserData(medio);
            opcion.setToggleGroup(medios);
            opcion.setSelected(medio == MedioPago.EFECTIVO);
            opciones.getChildren().add(opcion);
        }
        medios.selectedToggleProperty().addListener((obs, anterior, actual) -> {
            recibido.setDisable(medioElegido() != MedioPago.EFECTIVO);
            actualizarVuelto();
        });

        recibido.setPromptText("Vacío = total justo");
        recibido.textProperty().addListener((obs, anterior, actual) -> actualizarVuelto());
        recibido.setOnAction(e -> cobrar());

        Button botonCobrar = new Button("Cobrar");
        botonCobrar.setStyle("-fx-font-size: 18px;");
        botonCobrar.setMaxWidth(Double.MAX_VALUE);
        botonCobrar.setOnAction(e -> cobrar());

        Button botonCancelar = new Button("Cancelar venta");
        botonCancelar.setMaxWidth(Double.MAX_VALUE);
        botonCancelar.setOnAction(e -> cancelar());

        VBox cobro = new VBox(10,
                new Label("Total"), total,
                new Label("Medio de pago"), opciones,
                new Label("Recibido en efectivo"), recibido,
                vuelto, botonCobrar, botonCancelar);
        cobro.setPadding(new Insets(0, 0, 0, 16));
        cobro.setMinWidth(300);
        cobro.setPrefWidth(300);
        return cobro;
    }

    private void armarTabla() {
        tabla.getColumns().add(columna("Producto", 330, false,
                linea -> linea.nombre()));
        tabla.getColumns().add(columna("Cantidad", 90, true,
                linea -> String.valueOf(linea.cantidad())));
        tabla.getColumns().add(columna("Precio", 120, true,
                linea -> FormatoDinero.mostrar(linea.precioUnitario())));
        tabla.getColumns().add(columna("Subtotal", 130, true,
                linea -> FormatoDinero.mostrar(linea.subtotal())));
        tabla.setPlaceholder(new Label("Escaneá el primer producto para empezar la venta."));
    }

    private static TableColumn<LineaVenta, String> columna(String titulo, double ancho,
            boolean alinearALaDerecha, Function<LineaVenta, String> valor) {
        TableColumn<LineaVenta, String> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new ReadOnlyStringWrapper(valor.apply(celda.getValue())));
        if (alinearALaDerecha) {
            columna.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
        return columna;
    }

    private void escanear() {
        String texto = escaner.getText();
        Resultado resultado = presentador.escanear(texto);
        mostrar(resultado);
        if (resultado.exito()) {
            escaner.clear();
            recargar(new CodigoBarras(texto));
        } else {
            escaner.selectAll();
        }
    }

    private void sobreLaLineaElegida(Function<String, Resultado> accion) {
        LineaVenta linea = tabla.getSelectionModel().getSelectedItem();
        if (linea == null) {
            return;
        }
        mostrar(accion.apply(linea.codigo().valor()));
        recargar(null);
        escaner.requestFocus();
    }

    private void cobrar() {
        Resultado resultado = presentador.cobrar(medioElegido(), recibido.getText());
        mostrar(resultado);
        if (resultado.exito()) {
            reiniciarCobro();
            recargar(null);
        }
        escaner.requestFocus();
    }

    private void cancelar() {
        if (presentador.estaVacia()) {
            return;
        }
        if (confirmarCancelacion()) {
            mostrar(presentador.cancelar());
            reiniciarCobro();
            recargar(null);
        }
        escaner.requestFocus();
    }

    private boolean confirmarCancelacion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Se quitan todos los productos de esta venta. ¿Cancelarla?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Cancelar venta");
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(boton -> boton == ButtonType.YES).isPresent();
    }

    private void reiniciarCobro() {
        recibido.clear();
        medios.getToggles().stream()
                .filter(opcion -> opcion.getUserData() == MedioPago.EFECTIVO)
                .findFirst()
                .ifPresent(medios::selectToggle);
    }

    private MedioPago medioElegido() {
        Toggle elegido = medios.getSelectedToggle();
        return elegido == null ? MedioPago.EFECTIVO : (MedioPago) elegido.getUserData();
    }

    private void recargar(CodigoBarras aSeleccionar) {
        LineaVenta elegida = tabla.getSelectionModel().getSelectedItem();
        CodigoBarras codigoElegido = aSeleccionar != null
                ? aSeleccionar
                : (elegida == null ? null : elegida.codigo());

        tabla.getItems().setAll(presentador.lineas());
        if (codigoElegido != null) {
            for (int i = 0; i < tabla.getItems().size(); i++) {
                if (tabla.getItems().get(i).codigo().equals(codigoElegido)) {
                    tabla.getSelectionModel().select(i);
                    tabla.scrollTo(i);
                    break;
                }
            }
        }
        total.setText(FormatoDinero.mostrar(presentador.total()));
        actualizarVuelto();
    }

    private void actualizarVuelto() {
        vuelto.setText(presentador.textoDeVuelto(medioElegido(), recibido.getText()));
    }

    private void mostrar(Resultado resultado) {
        mensaje.setText(resultado.mensaje());
        mensaje.setStyle(resultado.exito() ? ESTILO_OK : ESTILO_ERROR);
    }
}
