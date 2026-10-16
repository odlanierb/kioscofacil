/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.resumen;

import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.ResumenDelDia;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.core.venta.VentaServicio;
import com.kioscofacil.ui.compartido.Resultado;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Lógica de la pantalla de resumen del día. Lee las ventas de la fecha elegida y
 * guarda el último cierre leído. No conoce JavaFX.
 */
public final class ResumenPresentador {
 
    /** Lo que se muestra de un día: los totales y las ventas, la más reciente primero. */
    public record Cierre(ResumenDelDia resumen, List<VentaRealizada> ventas) {
    }
 
    private static final System.Logger LOG =
            System.getLogger(ResumenPresentador.class.getName());
 
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
 
    private final VentaServicio ventas;
    private final Clock reloj;
    private Cierre cierre;
 
    public ResumenPresentador(VentaServicio ventas, Clock reloj) {
        this.ventas = Objects.requireNonNull(ventas);
        this.reloj = Objects.requireNonNull(reloj);
        this.cierre = new Cierre(ResumenDelDia.de(hoy(), List.of()), List.of());
    }
 
    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }
 
    /** El último cierre que se pudo leer. Al empezar es el de hoy, sin ventas. */
    public Cierre cierre() {
        return cierre;
    }
 
    /**
     * Lee las ventas de ese día. Si falla, devuelve un error y conserva el cierre anterior.
     */
    public Resultado cargar(LocalDate dia) {
        Objects.requireNonNull(dia);
        try {
            List<VentaRealizada> delDia = ventas.ventasDelDia(dia);
            cierre = new Cierre(ResumenDelDia.de(dia, delDia), List.copyOf(delDia.reversed()));
            return Resultado.ok("");
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "No se pudieron leer las ventas del " + dia, e);
            return Resultado.error("No se pudieron leer las ventas de ese día. Probá de nuevo; "
                    + "si sigue pasando, avisá a quien te instaló el programa.");
        }
    }
 
    /** Hora de la venta, por ejemplo "10:30". */
    public static String hora(VentaRealizada venta) {
        return venta.fecha().format(HORA);
    }
 
    /** Cantidad de unidades vendidas en la venta, sumando todas las líneas. */
    public static int articulos(VentaRealizada venta) {
        return venta.lineas().stream().mapToInt(LineaVenta::cantidad).sum();
    }
}
 