/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kioscofacil.core.compartido.EjecutorDirecto;
import com.kioscofacil.core.compartido.EjecutorTransaccional;
import java.util.function.Supplier;
import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.producto.ProductoRepositorioEnMemoria;
import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.stock.StockRepositorioEnMemoria;
import com.kioscofacil.core.stock.StockServicio;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class VentaServicioTest {

    private final VentaRepositorioEnMemoria repositorio = new VentaRepositorioEnMemoria();
    private final Clock reloj = Clock.fixed(
            Instant.parse("2026-10-11T15:00:00Z"), ZoneId.of("America/Argentina/Buenos_Aires"));
    private final ProductoServicio productos
            = new ProductoServicio(new ProductoRepositorioEnMemoria());
    private final StockServicio stock
            = new StockServicio(new StockRepositorioEnMemoria(), productos);
    private final VentaServicio servicio
            = new VentaServicio(repositorio, reloj, stock, new EjecutorDirecto());
    private final Producto alfajor
            = new Producto(new CodigoBarras("7790001000011"), "Alfajor", Dinero.de("1200"));

    private Venta ventaCobrada() {
        Venta venta = new Venta();
        venta.agregar(alfajor, 2);
        venta.cobrar(MedioPago.EFECTIVO, Dinero.de("5000"));
        return venta;
    }

    @Test
    void registraUnaVentaCobradaConLaFechaDelReloj() {
        VentaRealizada realizada = servicio.registrar(ventaCobrada());

        assertEquals(LocalDateTime.of(2026, 10, 11, 12, 0), realizada.fecha());
        assertEquals(Dinero.de("2400"), realizada.total());
        assertTrue(repositorio.buscarPorId(realizada.id()).isPresent());
    }

    @Test
    void noRegistraUnaVentaSinCobrar() {
        Venta venta = new Venta();
        venta.agregar(alfajor, 1);
        assertThrows(ReglaDeNegocioException.class, () -> servicio.registrar(venta));
    }

    @Test
    void noRegistraDosVecesLaMismaVenta() {
        Venta venta = ventaCobrada();
        servicio.registrar(venta);
        assertThrows(ReglaDeNegocioException.class, () -> servicio.registrar(venta));
    }

    @Test
    void ventasDelDiaDevuelveSoloLasDeEseDia() {
        Cobro cobro = new Cobro(MedioPago.EFECTIVO, Dinero.de("1200"), Dinero.CERO);
        List<LineaVenta> lineas = List.of(LineaVenta.de(alfajor, 1));
        VentaRealizada ayer = new VentaRealizada(
                UUID.randomUUID(), LocalDateTime.of(2026, 10, 10, 23, 59), lineas, cobro);
        VentaRealizada hoy = new VentaRealizada(
                UUID.randomUUID(), LocalDateTime.of(2026, 10, 11, 0, 0), lineas, cobro);
        repositorio.guardar(ayer);
        repositorio.guardar(hoy);

        assertEquals(List.of(hoy), servicio.ventasDelDia(LocalDate.of(2026, 10, 11)));
    }

    @Test
    void registrarUnaVentaDescuentaElStock() {
        productos.registrar(alfajor.codigo(), "Alfajor", Dinero.de("1200"));
        stock.ingresar(alfajor.codigo(), 10);

        servicio.registrar(ventaCobrada()); // vende 2

        assertEquals(8, stock.cantidadDe(alfajor.codigo()));
    }

    @Test
    void unaVentaSinStockCargadoSeRegistraIgualYElStockQuedaNegativo() {
        servicio.registrar(ventaCobrada()); // vende 2, sin stock cargado

        assertEquals(-2, stock.cantidadDe(alfajor.codigo()));
    }

    @Test
    void registrarSeEjecutaDentroDeLaTransaccion() {
        int[] ejecuciones = {0};
        EjecutorTransaccional contador = new EjecutorTransaccional() {
            @Override
            public <T> T ejecutar(Supplier<T> operacion) {
                ejecuciones[0]++;
                return operacion.get();
            }
        };
        VentaServicio conContador = new VentaServicio(repositorio, reloj, stock, contador);

        conContador.registrar(ventaCobrada());

        assertEquals(1, ejecuciones[0]);
    }
    
    
}
