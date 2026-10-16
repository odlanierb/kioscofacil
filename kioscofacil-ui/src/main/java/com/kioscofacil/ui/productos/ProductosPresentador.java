/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.productos;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.producto.ProductoDuplicadoException;
import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.stock.StockServicio;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Lógica de la pantalla de productos. No conoce JavaFX: recibe el texto tal
 * como lo escribió la persona, lo valida y llama a los servicios del core. Cada
 * acción devuelve un {@link Resultado} con un mensaje listo para mostrar.
 */
public final class ProductosPresentador {

    /**
     * Una fila de la tabla: el producto y su stock actual.
     */
    public record Fila(Producto producto, int stock) {

    }

    /**
     * Qué pasó al ejecutar una acción y qué mensaje mostrarle a la persona.
     */
    public record Resultado(boolean exito, String mensaje) {

        static Resultado ok(String mensaje) {
            return new Resultado(true, mensaje);
        }

        static Resultado error(String mensaje) {
            return new Resultado(false, mensaje);
        }
    }

    private static final System.Logger LOG
            = System.getLogger(ProductosPresentador.class.getName());

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");

    // Hasta 9 dígitos y, opcional, coma o punto con 1 o 2 decimales. "1.500" se rechaza
    // a propósito: no se puede saber si son mil quinientos o uno con cincuenta.
    private static final Pattern PRECIO = Pattern.compile("\\d{1,9}([.,]\\d{1,2})?");
    private static final Pattern CANTIDAD = Pattern.compile("\\d{1,6}");

    private static final String PRECIO_INVALIDO
            = "El precio tiene que ser un número, por ejemplo 1500 o 1500,50.";
    private static final String CANTIDAD_INVALIDA
            = "La cantidad tiene que ser un número entero, por ejemplo 12.";

    private final ProductoServicio productos;
    private final StockServicio stock;

    public ProductosPresentador(ProductoServicio productos, StockServicio stock) {
        this.productos = Objects.requireNonNull(productos);
        this.stock = Objects.requireNonNull(stock);
    }

    /**
     * Todos los productos con su stock, en el orden que devuelve el
     * repositorio.
     */
    public List<Fila> filas() {
        return productos.listar().stream()
                .map(producto -> new Fila(producto, stock.cantidadDe(producto.codigo())))
                .toList();
    }

    /**
     * Registra un producto nuevo. Si hay stock inicial (opcional), lo suma.
     */
    public Resultado registrar(String codigo, String nombre, String precio,
            String stockInicial) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            Dinero dinero = leerPrecio(precio);
            int unidades = leerCantidadOpcional(stockInicial);
            Producto producto = productos.registrar(codigoBarras, nombre, dinero);
            if (unidades > 0) {
                stock.ingresar(codigoBarras, unidades);
                return "Registraste «" + producto.nombre() + "» con stock " + unidades + ".";
            }
            return "Registraste «" + producto.nombre() + "».";
        });
    }

    public Resultado cambiarPrecio(String codigo, String precio) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            Producto actualizado = productos.cambiarPrecio(codigoBarras, leerPrecio(precio));
            return "El precio de «" + actualizado.nombre() + "» ahora es "
                    + formatearPrecio(actualizado.precio()) + ".";
        });
    }

    /**
     * Llegó mercadería: suma la cantidad al stock actual.
     */
    public Resultado sumarStock(String codigo, String cantidad) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            int unidades = leerCantidad(cantidad);
            Producto producto = productos.buscarPorCodigo(codigoBarras);
            stock.ingresar(codigoBarras, unidades);
            return "Sumaste " + describirUnidades(unidades) + " a «" + producto.nombre()
                    + "». Stock actual: " + stock.cantidadDe(codigoBarras) + ".";
        });
    }

    /**
     * Se contó la mercadería: deja el stock exactamente en la cantidad
     * indicada.
     */
    public Resultado fijarStock(String codigo, String cantidad) {
        return ejecutar(() -> {
            CodigoBarras codigoBarras = new CodigoBarras(codigo);
            int unidades = leerCantidad(cantidad);
            Producto producto = productos.buscarPorCodigo(codigoBarras);
            stock.ajustar(codigoBarras, unidades);
            return "El stock de «" + producto.nombre() + "» quedó en " + unidades + ".";
        });
    }

    /**
     * Precio para mostrar en la tabla, por ejemplo "$ 1.500,50".
     */
    public static String formatearPrecio(Dinero precio) {
        return String.format(ARGENTINA, "$ %,.2f", precio.monto());
    }

    /**
     * Precio para cargar en el campo de texto, por ejemplo "1500,50".
     */
    public static String precioParaEditar(Dinero precio) {
        return precio.monto().toPlainString().replace('.', ',');
    }

    private Resultado ejecutar(Supplier<String> accion) {
        try {
            return Resultado.ok(accion.get());
        } catch (ProductoDuplicadoException e) {
            return Resultado.error(e.getMessage() + " («" + e.existente().nombre() + "»).");
        } catch (ReglaDeNegocioException | IllegalArgumentException e) {
            return Resultado.error(e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Falló una acción de la pantalla de productos", e);
            return Resultado.error("No se pudo completar la acción. Probá de nuevo; "
                    + "si sigue pasando, avisá a quien te instaló el programa.");
        }
    }

    static Dinero leerPrecio(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.startsWith("$")) {
            limpio = limpio.substring(1).trim();
        }
        if (!PRECIO.matcher(limpio).matches()) {
            throw new IllegalArgumentException(PRECIO_INVALIDO);
        }
        return Dinero.de(limpio.replace(',', '.'));
    }

    static int leerCantidad(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (!CANTIDAD.matcher(limpio).matches()) {
            throw new IllegalArgumentException(CANTIDAD_INVALIDA);
        }
        return Integer.parseInt(limpio);
    }

    private static int leerCantidadOpcional(String texto) {
        return texto == null || texto.isBlank() ? 0 : leerCantidad(texto);
    }

    private static String describirUnidades(int cantidad) {
        return cantidad == 1 ? "1 unidad" : cantidad + " unidades";
    }
}
