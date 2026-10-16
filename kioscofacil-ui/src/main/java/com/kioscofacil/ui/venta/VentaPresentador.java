/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.venta;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.Venta;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.core.venta.VentaServicio;
import com.kioscofacil.ui.compartido.FormatoDinero;
import com.kioscofacil.ui.compartido.Resultado;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Lógica de la pantalla de venta. Maneja la venta en curso (el carrito) y la registra
 * al cobrar. No conoce JavaFX: recibe texto, valida y llama a los servicios del core.
 */
public final class VentaPresentador {
 
    private static final System.Logger LOG =
            System.getLogger(VentaPresentador.class.getName());
 
    private static final String RECIBIDO_INVALIDO =
            "El monto recibido tiene que ser un número, por ejemplo 2000 o 2000,50.";
 
    private final ProductoServicio productos;
    private final VentaServicio ventas;
    private Venta venta = new Venta();
 
    public VentaPresentador(ProductoServicio productos, VentaServicio ventas) {
        this.productos = Objects.requireNonNull(productos);
        this.ventas = Objects.requireNonNull(ventas);
    }
 
    public List<LineaVenta> lineas() {
        return venta.lineas();
    }
 
    public Dinero total() {
        return venta.total();
    }
 
    public boolean estaVacia() {
        return venta.estaVacia();
    }
 
    /** Suma una unidad del producto con ese código (lo que entra por el escáner). */
    public Resultado escanear(String codigo) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            Producto producto = buscarProducto(codigoBarras);
            venta.agregar(producto, 1);
            return "Agregaste «" + producto.nombre() + "».";
        });
    }
 
    public Resultado sumarUno(String codigo) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            venta.cambiarCantidad(codigoBarras, lineaDe(codigoBarras).cantidad() + 1);
            return "";
        });
    }
 
    /** Resta una unidad; si era la última, saca el producto de la venta. */
    public Resultado restarUno(String codigo) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            LineaVenta linea = lineaDe(codigoBarras);
            if (linea.cantidad() == 1) {
                venta.quitar(codigoBarras);
                return "Quitaste «" + linea.nombre() + "».";
            }
            venta.cambiarCantidad(codigoBarras, linea.cantidad() - 1);
            return "";
        });
    }
 
    public Resultado quitar(String codigo) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            LineaVenta linea = lineaDe(codigoBarras);
            venta.quitar(codigoBarras);
            return "Quitaste «" + linea.nombre() + "».";
        });
    }
 
    /**
     * Cobra y registra la venta. Con efectivo, un monto vacío significa "el total justo";
     * con cualquier otro medio se cobra siempre el total y el monto escrito se ignora.
     * Si falla al guardar, la venta queda cobrada pero abierta: cobrar de nuevo reintenta
     * guardarla con el mismo cobro, y cancelar empieza una venta nueva.
     */
    public Resultado cobrar(MedioPago medio, String recibido) {
        return ejecutar(() -> {
            if (medio == null) {
                throw new IllegalArgumentException("Elegí cómo paga el cliente.");
            }
            if (!venta.estaCobrada()) {
                venta.cobrar(medio, montoRecibido(medio, recibido));
            }
            VentaRealizada realizada = guardar();
            venta = new Venta();
            String mensaje = "Venta registrada: " + FormatoDinero.mostrar(realizada.total()) + ".";
            Dinero vuelto = realizada.cobro().vuelto();
            if (!vuelto.equals(Dinero.CERO)) {
                mensaje += " Vuelto: " + FormatoDinero.mostrar(vuelto) + ".";
            }
            return mensaje;
        });
    }
 
    /** Descarta la venta en curso y empieza una nueva. */
    public Resultado cancelar() {
        venta = new Venta();
        return Resultado.ok("Venta cancelada.");
    }
 
    /** Texto para mostrar mientras se escribe cuánto entregó el cliente. */
    public String textoDeVuelto(MedioPago medio, String recibido) {
        if (medio != MedioPago.EFECTIVO || venta.estaVacia()
                || recibido == null || recibido.isBlank()) {
            return "";
        }
        Dinero entregado;
        try {
            entregado = FormatoDinero.leer(recibido, RECIBIDO_INVALIDO);
        } catch (IllegalArgumentException e) {
            return "";
        }
        Dinero total = venta.total();
        if (entregado.esMenorQue(total)) {
            return "Faltan " + FormatoDinero.mostrar(total.restar(entregado));
        }
        return "Vuelto: " + FormatoDinero.mostrar(entregado.restar(total));
    }
 
    public static String nombreDe(MedioPago medio) {
        return switch (medio) {
            case EFECTIVO -> "Efectivo";
            case TRANSFERENCIA -> "Transferencia";
            case TARJETA -> "Tarjeta";
            case BILLETERA_VIRTUAL -> "Billetera virtual";
        };
    }
 
    private Producto buscarProducto(CodigoBarras codigo) {
        try {
            return productos.buscarPorCodigo(codigo);
        } catch (ReglaDeNegocioException e) {
            throw new ReglaDeNegocioException(
                    e.getMessage() + ". Registralo en la pestaña Productos.");
        }
    }
 
    private LineaVenta lineaDe(CodigoBarras codigo) {
        return venta.lineas().stream()
                .filter(linea -> linea.codigo().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "El producto " + codigo.valor() + " no está en la venta"));
    }
 
    private Dinero montoRecibido(MedioPago medio, String recibido) {
        if (medio != MedioPago.EFECTIVO || recibido == null || recibido.isBlank()) {
            return venta.total();
        }
        return FormatoDinero.leer(recibido, RECIBIDO_INVALIDO);
    }
 
    private VentaRealizada guardar() {
        try {
            return ventas.registrar(venta);
        } catch (ReglaDeNegocioException e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "No se pudo guardar la venta " + venta.id(), e);
            throw new ReglaDeNegocioException("No se pudo guardar la venta. Tocá «Cobrar» "
                    + "para reintentar o «Cancelar venta» para empezar de nuevo.");
        }
    }
 
    private Resultado ejecutar(Supplier<String> accion) {
        try {
            return Resultado.ok(accion.get());
        } catch (ReglaDeNegocioException | IllegalArgumentException e) {
            return Resultado.error(e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Falló una acción de la pantalla de venta", e);
            return Resultado.error("No se pudo completar la acción. Probá de nuevo; "
                    + "si sigue pasando, avisá a quien te instaló el programa.");
        }
    }
}
 