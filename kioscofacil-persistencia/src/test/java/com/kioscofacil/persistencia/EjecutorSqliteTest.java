/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.venta.Cobro;
import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.VentaRealizada;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class EjecutorSqliteTest {

    private BaseDeDatos baseDeDatos;
    private EjecutorSqlite ejecutor;
    private ProductoRepositorioSqlite productos;
    private StockRepositorioSqlite stock;
    private VentaRepositorioSqlite ventas;

    @BeforeEach
    void abrirBaseDeDatos() {
        baseDeDatos = BaseDeDatos.abrir("jdbc:sqlite::memory:");
        ejecutor = new EjecutorSqlite(baseDeDatos);
        productos = new ProductoRepositorioSqlite(baseDeDatos);
        stock = new StockRepositorioSqlite(baseDeDatos);
        ventas = new VentaRepositorioSqlite(baseDeDatos);
    }

    @AfterEach
    void cerrarBaseDeDatos() {
        baseDeDatos.close();
    }

    private static Producto alfajor() {
        return new Producto(new CodigoBarras("7790001"), "Alfajor", Dinero.de("850"));
    }

    private static VentaRealizada ventaExacta(LineaVenta... lineas) {
        Dinero total = Dinero.CERO;
        for (LineaVenta linea : lineas) {
            total = total.sumar(linea.subtotal());
        }
        return new VentaRealizada(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 10, 13, 12, 0, 0),
                List.of(lineas),
                new Cobro(MedioPago.EFECTIVO, total, Dinero.CERO));
    }

    @Test
    void devuelveElResultadoDeLaOperacion() {
        String resultado = ejecutor.ejecutar(() -> "listo");

        assertEquals("listo", resultado);
    }

    @Test
    void confirmaLosCambiosSiLaOperacionTerminaBien() {
        Producto producto = alfajor();

        ejecutor.ejecutar(() -> {
            productos.guardar(producto);
            return null;
        });

        assertEquals(Optional.of(producto), productos.buscarPorCodigo(producto.codigo()));
    }

    @Test
    void deshaceLosCambiosSiLaOperacionFalla() {
        Producto producto = alfajor();

        assertThrows(IllegalStateException.class, () -> ejecutor.ejecutar(() -> {
            productos.guardar(producto);
            throw new IllegalStateException("falla");
        }));

        assertEquals(Optional.empty(), productos.buscarPorCodigo(producto.codigo()));
    }

    @Test
    void relanzaLaMismaExcepcionSinEnvolverla() {
        IllegalStateException falla = new IllegalStateException("falla");

        IllegalStateException lanzada = assertThrows(IllegalStateException.class,
                () -> ejecutor.ejecutar(() -> {
                    throw falla;
                }));

        assertSame(falla, lanzada);
    }

    @Test
    void restauraElAutoCommitDespuesDeConfirmar() throws SQLException {
        ejecutor.ejecutar(() -> "listo");

        assertTrue(baseDeDatos.conexion().getAutoCommit());
    }

    @Test
    void restauraElAutoCommitDespuesDeUnFallo() throws SQLException {
        assertThrows(IllegalStateException.class, () -> ejecutor.ejecutar(() -> {
            throw new IllegalStateException("falla");
        }));

        assertTrue(baseDeDatos.conexion().getAutoCommit());
    }

    @Test
    void seUneALaTransaccionEnCursoEnVezDeConfirmarPorSuCuenta() {
        Producto producto = alfajor();

        assertThrows(IllegalStateException.class, () -> ejecutor.ejecutar(() -> {
            ejecutor.ejecutar(() -> {
                productos.guardar(producto);
                return null;
            });
            throw new IllegalStateException("falla");
        }));

        assertEquals(Optional.empty(), productos.buscarPorCodigo(producto.codigo()));
    }

    @Test
    void unaFallaALaMitadDeUnaVentaDejaVentaYStockIntactos() {
        CodigoBarras alfajor = new CodigoBarras("7790001");
        CodigoBarras inexistente = new CodigoBarras("999");
        productos.guardar(new Producto(alfajor, "Alfajor", Dinero.de("850")));
        stock.guardar(alfajor, 10);
        VentaRealizada venta = ventaExacta(
                new LineaVenta(alfajor, "Alfajor", Dinero.de("850"), 1),
                new LineaVenta(inexistente, "Producto fantasma", Dinero.de("100"), 1));

        assertThrows(PersistenciaException.class, () -> ejecutor.ejecutar(() -> {
            ventas.guardar(venta);
            stock.guardar(alfajor, 9);
            stock.guardar(inexistente, -1);
            return null;
        }));

        assertEquals(Optional.empty(), ventas.buscarPorId(venta.id()));
        assertEquals(10, stock.cantidadDe(alfajor));
    }
}
