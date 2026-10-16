/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.resumen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.EjecutorTransaccional;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.producto.ProductoRepositorio;
import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.stock.StockRepositorio;
import com.kioscofacil.core.stock.StockServicio;
import com.kioscofacil.core.venta.Cobro;
import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.core.venta.VentaRepositorio;
import com.kioscofacil.core.venta.VentaServicio;
import com.kioscofacil.ui.compartido.Resultado;
import com.kioscofacil.ui.resumen.ResumenPresentador.Cierre;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class ResumenPresentadorTest {
 
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final LocalDate AYER = LocalDate.of(2026, 10, 14);
    private static final CodigoBarras ALFAJOR = new CodigoBarras("7790001");
    private static final CodigoBarras AGUA = new CodigoBarras("7790002");
 
    private VentasEnMemoria repositorio;
    private ResumenPresentador presentador;
 
    @BeforeEach
    void preparar() {
        repositorio = new VentasEnMemoria();
        ProductoServicio productos = new ProductoServicio(new ProductosVacios());
        StockServicio stock = new StockServicio(new StockVacio(), productos);
        Clock reloj = Clock.fixed(Instant.parse("2026-10-15T15:00:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires"));
        VentaServicio ventas = new VentaServicio(repositorio, reloj, stock, new EjecutorDirecto());
        presentador = new ResumenPresentador(ventas, reloj);
    }
 
    @Test
    void alEmpezarMuestraHoySinVentas() {
        Cierre cierre = presentador.cierre();
 
        assertEquals(HOY, presentador.hoy());
        assertEquals(HOY, cierre.resumen().dia());
        assertEquals(0, cierre.resumen().cantidadDeVentas());
        assertEquals(Dinero.CERO, cierre.resumen().total());
        assertTrue(cierre.ventas().isEmpty());
    }
 
    @Test
    void cargarUnDiaDaLosTotalesYElDesglosePorMedioDePago() {
        repositorio.agregar(venta(HOY, 10, 30, MedioPago.EFECTIVO, "5000", linea(ALFAJOR, "Alfajor", "1500", 2)));
        repositorio.agregar(venta(HOY, 11, 45, MedioPago.TARJETA, "900", linea(AGUA, "Agua", "900", 1)));
        repositorio.agregar(venta(HOY, 18, 5, MedioPago.EFECTIVO, "1500", linea(ALFAJOR, "Alfajor", "1500", 1)));
 
        Resultado resultado = presentador.cargar(HOY);
 
        assertTrue(resultado.exito());
        Cierre cierre = presentador.cierre();
        assertEquals(3, cierre.resumen().cantidadDeVentas());
        assertEquals(Dinero.de("5400"), cierre.resumen().total());
        assertEquals(Dinero.de("4500"), cierre.resumen().totalPorMedio().get(MedioPago.EFECTIVO));
        assertEquals(Dinero.de("900"), cierre.resumen().totalPorMedio().get(MedioPago.TARJETA));
        assertEquals(Dinero.CERO, cierre.resumen().totalPorMedio().get(MedioPago.TRANSFERENCIA));
    }
 
    @Test
    void elVueltoNoCuentaComoVendido() {
        repositorio.agregar(venta(HOY, 9, 0, MedioPago.EFECTIVO, "10000", linea(AGUA, "Agua", "900", 1)));
 
        presentador.cargar(HOY);
 
        assertEquals(Dinero.de("900"), presentador.cierre().resumen().total());
    }
 
    @Test
    void losDemasDiasNoSeMezclan() {
        repositorio.agregar(venta(AYER, 20, 0, MedioPago.EFECTIVO, "900", linea(AGUA, "Agua", "900", 1)));
        repositorio.agregar(venta(HOY, 10, 0, MedioPago.EFECTIVO, "1500", linea(ALFAJOR, "Alfajor", "1500", 1)));
 
        presentador.cargar(AYER);
 
        assertEquals(1, presentador.cierre().resumen().cantidadDeVentas());
        assertEquals(AYER, presentador.cierre().resumen().dia());
        assertEquals(Dinero.de("900"), presentador.cierre().resumen().total());
    }
 
    @Test
    void lasVentasSeListanDeLaMasRecienteALaMasVieja() {
        repositorio.agregar(venta(HOY, 10, 30, MedioPago.EFECTIVO, "1500", linea(ALFAJOR, "Alfajor", "1500", 1)));
        repositorio.agregar(venta(HOY, 18, 5, MedioPago.TARJETA, "900", linea(AGUA, "Agua", "900", 1)));
        repositorio.agregar(venta(HOY, 12, 0, MedioPago.EFECTIVO, "1500", linea(ALFAJOR, "Alfajor", "1500", 1)));
 
        presentador.cargar(HOY);
 
        List<String> horas = presentador.cierre().ventas().stream()
                .map(ResumenPresentador::hora).toList();
        assertEquals(List.of("18:05", "12:00", "10:30"), horas);
    }
 
    @Test
    void laHoraYLosArticulosSeCalculanDesdeLaVenta() {
        VentaRealizada venta = venta(HOY, 9, 7, MedioPago.EFECTIVO, "6000",
                linea(ALFAJOR, "Alfajor", "1500", 2), linea(AGUA, "Agua", "900", 3));
 
        assertEquals("09:07", ResumenPresentador.hora(venta));
        assertEquals(5, ResumenPresentador.articulos(venta));
    }
 
    @Test
    void siFallaLaLecturaDaUnErrorYConservaElCierreAnterior() {
        repositorio.agregar(venta(HOY, 10, 0, MedioPago.EFECTIVO, "1500", linea(ALFAJOR, "Alfajor", "1500", 1)));
        presentador.cargar(HOY);
        repositorio.fallarAlLeer = true;
 
        Resultado resultado = presentador.cargar(AYER);
 
        assertFalse(resultado.exito());
        assertFalse(resultado.mensaje().isBlank());
        assertEquals(HOY, presentador.cierre().resumen().dia());
        assertEquals(1, presentador.cierre().resumen().cantidadDeVentas());
    }
 
    private static LineaVenta linea(CodigoBarras codigo, String nombre, String precio, int cantidad) {
        return new LineaVenta(codigo, nombre, Dinero.de(precio), cantidad);
    }
 
    private static VentaRealizada venta(LocalDate dia, int hora, int minuto, MedioPago medio,
            String recibido, LineaVenta... lineas) {
        Dinero total = Dinero.CERO;
        for (LineaVenta linea : lineas) {
            total = total.sumar(linea.subtotal());
        }
        Dinero dineroRecibido = Dinero.de(recibido);
        Cobro cobro = new Cobro(medio, dineroRecibido, dineroRecibido.restar(total));
        return new VentaRealizada(UUID.randomUUID(), dia.atTime(hora, minuto),
                List.of(lineas), cobro);
    }
 
    private static final class VentasEnMemoria implements VentaRepositorio {
 
        private final List<VentaRealizada> guardadas = new ArrayList<>();
        private boolean fallarAlLeer;
 
        void agregar(VentaRealizada venta) {
            guardadas.add(venta);
        }
 
        @Override
        public void guardar(VentaRealizada venta) {
            guardadas.add(venta);
        }
 
        @Override
        public Optional<VentaRealizada> buscarPorId(UUID id) {
            return guardadas.stream().filter(venta -> venta.id().equals(id)).findFirst();
        }
 
        @Override
        public List<VentaRealizada> listarEntre(LocalDateTime desde, LocalDateTime hasta) {
            if (fallarAlLeer) {
                throw new IllegalStateException("Falla simulada de la base de datos");
            }
            return guardadas.stream()
                    .filter(venta -> !venta.fecha().isBefore(desde) && venta.fecha().isBefore(hasta))
                    .sorted((a, b) -> a.fecha().compareTo(b.fecha()))
                    .toList();
        }
    }
 
    private static final class ProductosVacios implements ProductoRepositorio {
 
        @Override
        public void guardar(Producto producto) {
        }
 
        @Override
        public Optional<Producto> buscarPorCodigo(CodigoBarras codigo) {
            return Optional.empty();
        }
 
        @Override
        public List<Producto> listarTodos() {
            return List.of();
        }
    }
 
    private static final class StockVacio implements StockRepositorio {
 
        @Override
        public int cantidadDe(CodigoBarras codigo) {
            return 0;
        }
 
        @Override
        public void guardar(CodigoBarras codigo, int cantidad) {
        }
    }
 
    private static final class EjecutorDirecto implements EjecutorTransaccional {
 
        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }
}
 