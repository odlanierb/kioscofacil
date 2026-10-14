/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.producto.ProductoDuplicadoException;
import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.stock.StockServicio;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.Venta;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.core.venta.VentaServicio;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Prueba los servicios reales del core sobre los repositorios SQLite reales.
 * Acá productos, stock y ventas son los servicios; los repositorios se nombran aparte.
 */
class IntegracionConServiciosTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    // 15:30 UTC es 12:30 en Buenos Aires
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-13T15:30:00Z"), ZONA);

    private BaseDeDatos baseDeDatos;
    private EjecutorSqlite ejecutor;
    private VentaRepositorioSqlite repositorioDeVentas;
    private ProductoServicio productos;
    private StockServicio stock;
    private VentaServicio ventas;

    @BeforeEach
    void armarTodo() {
        baseDeDatos = BaseDeDatos.abrir("jdbc:sqlite::memory:");
        ejecutor = new EjecutorSqlite(baseDeDatos);
        repositorioDeVentas = new VentaRepositorioSqlite(baseDeDatos);
        productos = new ProductoServicio(new ProductoRepositorioSqlite(baseDeDatos));
        stock = new StockServicio(new StockRepositorioSqlite(baseDeDatos), productos);
        ventas = new VentaServicio(repositorioDeVentas, RELOJ, stock, ejecutor);
    }

    @AfterEach
    void cerrarBaseDeDatos() {
        baseDeDatos.close();
    }

    private Producto registrarAlfajor(int stockInicial) {
        Producto alfajor = productos.registrar(
                new CodigoBarras("7790001"), "Alfajor", Dinero.de("850"));
        stock.ingresar(alfajor.codigo(), stockInicial);
        return alfajor;
    }

    private static Venta ventaCobrada(Producto producto, int cantidad) {
        Venta venta = new Venta();
        venta.agregar(producto, cantidad);
        venta.cobrar(MedioPago.TARJETA, venta.total());
        return venta;
    }

    @Test
    void registrarUnProductoDuplicadoFallaYReemplazarLoActualiza() {
        CodigoBarras codigo = new CodigoBarras("7790001");
        Producto original = productos.registrar(codigo, "Alfajor", Dinero.de("850"));

        ProductoDuplicadoException duplicado = assertThrows(ProductoDuplicadoException.class,
                () -> productos.registrar(codigo, "Otro nombre", Dinero.de("100")));
        assertEquals(original, duplicado.existente());

        productos.reemplazar(codigo, "Alfajor triple", Dinero.de("1200"));

        assertEquals("Alfajor triple", productos.buscarPorCodigo(codigo).nombre());
        assertEquals(1, productos.listar().size());
    }

    @Test
    void registrarUnaVentaLaGuardaYDescuentaElStock() {
        Producto alfajor = registrarAlfajor(10);
        Venta venta = new Venta();
        venta.agregar(alfajor, 2);
        venta.cobrar(MedioPago.EFECTIVO, Dinero.de("2000"));

        VentaRealizada realizada = ventas.registrar(venta);

        assertEquals(Optional.of(realizada), repositorioDeVentas.buscarPorId(venta.id()));
        assertEquals(Dinero.de("300"), realizada.cobro().vuelto());
        assertEquals(8, stock.cantidadDe(alfajor.codigo()));
    }

    @Test
    void registrarLaMismaVentaDosVecesFallaSinDescontarOtraVez() {
        Producto alfajor = registrarAlfajor(10);
        Venta venta = ventaCobrada(alfajor, 2);
        ventas.registrar(venta);

        assertThrows(ReglaDeNegocioException.class, () -> ventas.registrar(venta));

        assertEquals(8, stock.cantidadDe(alfajor.codigo()));
    }

    @Test
    void unaFallaAlDescontarElStockDeshaceLaVentaCompleta() {
        Producto alfajor = registrarAlfajor(10);
        Producto noRegistrado = new Producto(
                new CodigoBarras("999"), "Producto fantasma", Dinero.de("100"));
        Venta venta = new Venta();
        venta.agregar(alfajor, 1);
        venta.agregar(noRegistrado, 1);
        venta.cobrar(MedioPago.TARJETA, venta.total());

        assertThrows(PersistenciaException.class, () -> ventas.registrar(venta));

        assertEquals(Optional.empty(), repositorioDeVentas.buscarPorId(venta.id()));
        assertEquals(10, stock.cantidadDe(alfajor.codigo()));
    }

    @Test
    void cambiarElPrecioNoAlteraLasVentasYaRegistradas() {
        Producto alfajor = registrarAlfajor(10);
        VentaRealizada realizada = ventas.registrar(ventaCobrada(alfajor, 1));

        productos.cambiarPrecio(alfajor.codigo(), Dinero.de("1000"));

        VentaRealizada guardada = repositorioDeVentas.buscarPorId(realizada.id()).orElseThrow();
        assertEquals(Dinero.de("850"), guardada.lineas().get(0).precioUnitario());
        assertEquals(Dinero.de("1000"), productos.buscarPorCodigo(alfajor.codigo()).precio());
    }

    @Test
    void ventasDelDiaDevuelveSoloLasVentasDeEseDia() {
        Producto alfajor = registrarAlfajor(10);
        Clock relojDeManana = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZONA);
        VentaServicio ventasDeManana =
                new VentaServicio(repositorioDeVentas, relojDeManana, stock, ejecutor);
        VentaRealizada deHoy = ventas.registrar(ventaCobrada(alfajor, 1));
        ventasDeManana.registrar(ventaCobrada(alfajor, 1));

        List<VentaRealizada> ventasDeHoy = ventas.ventasDelDia(LocalDate.of(2026, 10, 13));

        assertEquals(List.of(deHoy), ventasDeHoy);
    }
    
        @Test
    void elResumenDelDiaSumaLasVentasRegistradasPorMedioDePago() {
        Producto alfajor = registrarAlfajor(10);
        Venta enEfectivo = new Venta();
        enEfectivo.agregar(alfajor, 2);
        enEfectivo.cobrar(MedioPago.EFECTIVO, Dinero.de("2000"));
        ventas.registrar(enEfectivo);
        ventas.registrar(ventaCobrada(alfajor, 1));

        var resumen = ventas.resumenDelDia(LocalDate.of(2026, 10, 13));

        assertEquals(2, resumen.cantidadDeVentas());
        assertEquals(Dinero.de("2550"), resumen.total());
        assertEquals(Dinero.de("1700"), resumen.totalPorMedio().get(MedioPago.EFECTIVO));
        assertEquals(Dinero.de("850"), resumen.totalPorMedio().get(MedioPago.TARJETA));
    }
}
