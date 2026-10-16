/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.resumen;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.ResumenDelDia;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.ui.compartido.FormatoDinero;
import com.kioscofacil.ui.compartido.Resultado;
import com.kioscofacil.ui.resumen.ResumenPresentador.Cierre;
import com.kioscofacil.ui.venta.VentaPresentador;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Pantalla con lo vendido en un día: total, desglose por medio de pago y la lista de
 * ventas. Solo arma los controles y muestra lo que prepara el {@link ResumenPresentador}.
 */
public final class PantallaResumen {
 
    private static final String ESTILO_ERROR = "-fx-text-fill: #b3261e;";
 
    private final ResumenPresentador presentador;
 
    private final DatePicker selectorDeDia = new DatePicker();
    private final Label total = new Label();
    private final Label cantidad = new Label();
    private final GridPane porMedio = new GridPane();
    private final Label mensaje = new Label();
    private final TableView<VentaRealizada> tabla = new TableView<>();
    private final VBox raiz = new VBox(12);
 
    public PantallaResumen(ResumenPresentador presentador) {
        this.presentador = Objects.requireNonNull(presentador);
 
        armarTabla();
        total.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        cantidad.setStyle("-fx-font-size: 16px;");
        porMedio.setHgap(24);
        porMedio.setVgap(6);
        mensaje.setWrapText(true);
        mensaje.setMinHeight(24);
        mensaje.setStyle(ESTILO_ERROR);
 
        selectorDeDia.setEditable(false);
        selectorDeDia.setValue(presentador.hoy());
        selectorDeDia.valueProperty().addListener((obs, anterior, actual) -> actualizar());
 
        raiz.setPadding(new Insets(16));
        raiz.setStyle("-fx-font-size: 14px;");
        raiz.getChildren().addAll(armarSelector(), mensaje, armarTotales(), tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
 
        actualizar();
    }
 
    public Parent raiz() {
        return raiz;
    }
 
    /** Vuelve a leer las ventas del día elegido, por ejemplo al entrar a la pestaña. */
    public void actualizar() {
        LocalDate dia = selectorDeDia.getValue();
        if (dia == null) {
            return;
        }
        Resultado resultado = presentador.cargar(dia);
        mensaje.setText(resultado.mensaje());
        mostrar(presentador.cierre());
    }
 
    private HBox armarSelector() {
        Button hoy = new Button("Hoy");
        hoy.setOnAction(e -> irAHoy());
        Button recargar = new Button("Actualizar");
        recargar.setOnAction(e -> actualizar());
 
        HBox selector = new HBox(8, new Label("Día"), selectorDeDia, hoy, recargar);
        selector.setAlignment(Pos.CENTER_LEFT);
        return selector;
    }
 
    private HBox armarTotales() {
        VBox vendido = new VBox(4, new Label("Total vendido"), total, cantidad);
        VBox medios = new VBox(6, new Label("Por medio de pago"), porMedio);
        return new HBox(48, vendido, medios);
    }
 
    private void armarTabla() {
        tabla.getColumns().add(columna("Hora", 90, false,
                venta -> ResumenPresentador.hora(venta)));
        tabla.getColumns().add(columna("Medio de pago", 200, false,
                venta -> VentaPresentador.nombreDe(venta.cobro().medio())));
        tabla.getColumns().add(columna("Artículos", 100, true,
                venta -> String.valueOf(ResumenPresentador.articulos(venta))));
        tabla.getColumns().add(columna("Total", 150, true,
                venta -> FormatoDinero.mostrar(venta.total())));
        tabla.setPlaceholder(new Label("No hay ventas registradas ese día."));
    }
 
    private static TableColumn<VentaRealizada, String> columna(String titulo, double ancho,
            boolean alinearALaDerecha, Function<VentaRealizada, String> valor) {
        TableColumn<VentaRealizada, String> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new ReadOnlyStringWrapper(valor.apply(celda.getValue())));
        if (alinearALaDerecha) {
            columna.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
        return columna;
    }
 
    private void irAHoy() {
        LocalDate hoy = presentador.hoy();
        if (hoy.equals(selectorDeDia.getValue())) {
            actualizar();
        } else {
            selectorDeDia.setValue(hoy); // el cambio de valor dispara actualizar()
        }
    }
 
    private void mostrar(Cierre cierre) {
        ResumenDelDia resumen = cierre.resumen();
        total.setText(FormatoDinero.mostrar(resumen.total()));
        cantidad.setText(resumen.cantidadDeVentas() == 1
                ? "1 venta" : resumen.cantidadDeVentas() + " ventas");
 
        porMedio.getChildren().clear();
        int fila = 0;
        for (MedioPago medio : MedioPago.values()) {
            Dinero monto = resumen.totalPorMedio().getOrDefault(medio, Dinero.CERO);
            Label valor = new Label(FormatoDinero.mostrar(monto));
            valor.setMaxWidth(Double.MAX_VALUE);
            valor.setAlignment(Pos.CENTER_RIGHT);
            GridPane.setHgrow(valor, Priority.ALWAYS);
            porMedio.addRow(fila++, new Label(VentaPresentador.nombreDe(medio)), valor);
        }
 
        tabla.getItems().setAll(cierre.ventas());
    }
}
 