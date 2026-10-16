/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.venta;

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
import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.core.venta.VentaRepositorio;
import com.kioscofacil.core.venta.VentaServicio;
import com.kioscofacil.ui.compartido.Resultado;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class VentaPresentadorTest {
 
    private static final String ALFAJOR = "7790001";
    private static final String AGUA = "7790002";
 
    private StockEnMemoria stock;
    private VentasEnMemoria ventasGuardadas;
    private VentaPresentador presentador;
 
    @BeforeEach
    void preparar() {
        ProductosEnMemoria repositorioDeProductos = new ProductosEnMemoria();
        stock = new StockEnMemoria();
        ventasGuardadas = new VentasEnMemoria();
        ProductoServicio productos = new ProductoServicio(repositorioDeProductos);
        StockServicio servicioDeStock = new StockServicio(stock, productos);
        Clock reloj = Clock.fixed(Instant.parse("2026-10-15T15:00:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires"));
        VentaServicio ventas = new VentaServicio(
                ventasGuardadas, reloj, servicioDeStock, new EjecutorDirecto());
        presentador = new VentaPresentador(productos, ventas);
 
        productos.registrar(new CodigoBarras(ALFAJOR), "Alfajor", Dinero.de("1500"));
        productos.registrar(new CodigoBarras(AGUA), "Agua", Dinero.de("900"));
    }
 
    @Test
    void escanearAgregaElProductoYSumaSiSeRepite() {
        assertTrue(presentador.escanear(ALFAJOR).exito());
        assertTrue(presentador.escanear(ALFAJOR).exito());
        assertTrue(presentador.escanear(AGUA).exito());
 
        List<LineaVenta> lineas = presentador.lineas();
        assertEquals(2, lineas.size());
        assertEquals(2, lineas.get(0).cantidad());
        assertEquals(Dinero.de("3900"), presentador.total());
    }
 
    @Test
    void escanearUnCodigoQueNoExisteExplicaQueHacer() {
        Resultado resultado = presentador.escanear("0000");
 
        assertFalse(resultado.exito());
        assertTrue(resultado.mensaje().contains("0000"));
        assertTrue(resultado.mensaje().contains("Productos"));
        assertTrue(presentador.estaVacia());
    }
 
    @Test
    void escanearSinCodigoDaError() {
        assertFalse(presentador.escanear("   ").exito());
        assertTrue(presentador.estaVacia());
    }
 
    @Test
    void sumarUnoYRestarUnoCambianLaCantidad() {
        presentador.escanear(ALFAJOR);
 
        presentador.sumarUno(ALFAJOR);
        assertEquals(2, presentador.lineas().get(0).cantidad());
 
        presentador.restarUno(ALFAJOR);
        assertEquals(1, presentador.lineas().get(0).cantidad());
    }
 
    @Test
    void restarUnoDeLaUltimaUnidadSacaElProducto() {
        presentador.escanear(ALFAJOR);
 
        Resultado resultado = presentador.restarUno(ALFAJOR);
 
        assertTrue(resultado.exito());
        assertTrue(presentador.estaVacia());
    }
 
    @Test
    void quitarSacaLaLineaCompleta() {
        presentador.escanear(ALFAJOR);
        presentador.escanear(ALFAJOR);
        presentador.escanear(AGUA);
 
        presentador.quitar(ALFAJOR);
 
        assertEquals(1, presentador.lineas().size());
        assertEquals(Dinero.de("900"), presentador.total());
    }
 
    @Test
    void quitarUnProductoQueNoEstaEnLaVentaDaError() {
        assertFalse(presentador.quitar(ALFAJOR).exito());
        assertFalse(presentador.sumarUno(ALFAJOR).exito());
    }
 
    @Test
    void cobrarEnEfectivoConVueltoRegistraLaVentaYEmpiezaOtra() {
        presentador.escanear(ALFAJOR);
        presentador.escanear(ALFAJOR);
 
        Resultado resultado = presentador.cobrar(MedioPago.EFECTIVO, "5000");
 
        assertTrue(resultado.exito());
        assertTrue(resultado.mensaje().contains("$ 3.000,00"));
        assertTrue(resultado.mensaje().contains("Vuelto: $ 2.000,00"));
        assertEquals(1, ventasGuardadas.guardadas.size());
        assertEquals(Dinero.de("2000"), ventasGuardadas.guardadas.get(0).cobro().vuelto());
        assertTrue(presentador.estaVacia());
        assertEquals(-2, stock.cantidadDe(new CodigoBarras(ALFAJOR)));
    }
 
    @Test
    void cobrarEnEfectivoSinMontoCobraElTotalJusto() {
        presentador.escanear(AGUA);
 
        Resultado resultado = presentador.cobrar(MedioPago.EFECTIVO, "");
 
        assertTrue(resultado.exito());
        assertFalse(resultado.mensaje().contains("Vuelto"));
        assertEquals(Dinero.CERO, ventasGuardadas.guardadas.get(0).cobro().vuelto());
    }
 
    @Test
    void cobrarConTarjetaIgnoraElMontoEscrito() {
        presentador.escanear(AGUA);
 
        Resultado resultado = presentador.cobrar(MedioPago.TARJETA, "9999");
 
        assertTrue(resultado.exito());
        VentaRealizada guardada = ventasGuardadas.guardadas.get(0);
        assertEquals(MedioPago.TARJETA, guardada.cobro().medio());
        assertEquals(Dinero.de("900"), guardada.cobro().recibido());
    }
 
    @Test
    void cobrarConUnMontoQueNoAlcanzaNoRegistraNadaYLaVentaSigueAbierta() {
        presentador.escanear(ALFAJOR);
 
        Resultado resultado = presentador.cobrar(MedioPago.EFECTIVO, "1000");
 
        assertFalse(resultado.exito());
        assertTrue(ventasGuardadas.guardadas.isEmpty());
        assertTrue(presentador.escanear(AGUA).exito());
        assertEquals(2, presentador.lineas().size());
    }
 
    @Test
    void cobrarConUnMontoInvalidoDaError() {
        presentador.escanear(ALFAJOR);
 
        assertFalse(presentador.cobrar(MedioPago.EFECTIVO, "mucho").exito());
        assertTrue(ventasGuardadas.guardadas.isEmpty());
    }
 
    @Test
    void cobrarUnaVentaVaciaDaError() {
        assertFalse(presentador.cobrar(MedioPago.EFECTIVO, "").exito());
        assertTrue(ventasGuardadas.guardadas.isEmpty());
    }
 
    @Test
    void despuesDeCobrarSePuedeHacerOtraVenta() {
        presentador.escanear(ALFAJOR);
        presentador.cobrar(MedioPago.EFECTIVO, "");
 
        presentador.escanear(AGUA);
        presentador.cobrar(MedioPago.TRANSFERENCIA, "");
 
        assertEquals(2, ventasGuardadas.guardadas.size());
        assertFalse(ventasGuardadas.guardadas.get(0).id()
                .equals(ventasGuardadas.guardadas.get(1).id()));
    }
 
    @Test
    void siFallaAlGuardarSePuedeReintentarYLaVentaSeGuardaUnaSolaVez() {
        presentador.escanear(ALFAJOR);
        ventasGuardadas.fallarAlGuardar = true;
 
        Resultado fallo = presentador.cobrar(MedioPago.EFECTIVO, "2000");
 
        assertFalse(fallo.exito());
        assertTrue(fallo.mensaje().contains("reintentar"));
        assertTrue(ventasGuardadas.guardadas.isEmpty());
 
        ventasGuardadas.fallarAlGuardar = false;
        Resultado reintento = presentador.cobrar(MedioPago.EFECTIVO, "2000");
 
        assertTrue(reintento.exito());
        assertEquals(1, ventasGuardadas.guardadas.size());
        assertTrue(presentador.estaVacia());
    }
 
    @Test
    void despuesDeUnFalloAlGuardarSePuedeCancelarYEmpezarDeNuevo() {
        presentador.escanear(ALFAJOR);
        ventasGuardadas.fallarAlGuardar = true;
        presentador.cobrar(MedioPago.EFECTIVO, "2000");
 
        presentador.cancelar();
 
        assertTrue(presentador.estaVacia());
        assertTrue(presentador.escanear(AGUA).exito());
    }
 
    @Test
    void cancelarVaciaLaVenta() {
        presentador.escanear(ALFAJOR);
 
        Resultado resultado = presentador.cancelar();
 
        assertTrue(resultado.exito());
        assertTrue(presentador.estaVacia());
        assertEquals(Dinero.CERO, presentador.total());
    }
 
    @Test
    void elTextoDeVueltoSigueLoQueSeVaEscribiendo() {
        presentador.escanear(ALFAJOR);
        presentador.escanear(ALFAJOR);
 
        assertEquals("Vuelto: $ 2.000,00",
                presentador.textoDeVuelto(MedioPago.EFECTIVO, "5000"));
        assertEquals("Faltan $ 2.000,00",
                presentador.textoDeVuelto(MedioPago.EFECTIVO, "1000"));
        assertEquals("Vuelto: $ 0,00",
                presentador.textoDeVuelto(MedioPago.EFECTIVO, "3000"));
        assertEquals("", presentador.textoDeVuelto(MedioPago.EFECTIVO, ""));
        assertEquals("", presentador.textoDeVuelto(MedioPago.EFECTIVO, "abc"));
        assertEquals("", presentador.textoDeVuelto(MedioPago.TARJETA, "5000"));
    }
 
    @Test
    void cadaMedioDePagoTieneUnNombreParaMostrar() {
        for (MedioPago medio : MedioPago.values()) {
            assertFalse(VentaPresentador.nombreDe(medio).isBlank());
        }
    }
 
    private static final class ProductosEnMemoria implements ProductoRepositorio {
 
        private final Map<CodigoBarras, Producto> datos = new LinkedHashMap<>();
 
        @Override
        public void guardar(Producto producto) {
            datos.put(producto.codigo(), producto);
        }
 
        @Override
        public Optional<Producto> buscarPorCodigo(CodigoBarras codigo) {
            return Optional.ofNullable(datos.get(codigo));
        }
 
        @Override
        public List<Producto> listarTodos() {
            return new ArrayList<>(datos.values());
        }
    }
 
    private static final class StockEnMemoria implements StockRepositorio {
 
        private final Map<CodigoBarras, Integer> datos = new HashMap<>();
 
        @Override
        public int cantidadDe(CodigoBarras codigo) {
            return datos.getOrDefault(codigo, 0);
        }
 
        @Override
        public void guardar(CodigoBarras codigo, int cantidad) {
            datos.put(codigo, cantidad);
        }
    }
 
    private static final class VentasEnMemoria implements VentaRepositorio {
 
        private final List<VentaRealizada> guardadas = new ArrayList<>();
        private boolean fallarAlGuardar;
 
        @Override
        public void guardar(VentaRealizada venta) {
            if (fallarAlGuardar) {
                throw new IllegalStateException("Falla simulada de la base de datos");
            }
            guardadas.add(venta);
        }
 
        @Override
        public Optional<VentaRealizada> buscarPorId(UUID id) {
            return guardadas.stream().filter(venta -> venta.id().equals(id)).findFirst();
        }
 
        @Override
        public List<VentaRealizada> listarEntre(LocalDateTime desde, LocalDateTime hasta) {
            return guardadas.stream()
                    .filter(venta -> !venta.fecha().isBefore(desde) && venta.fecha().isBefore(hasta))
                    .toList();
        }
    }
 
    private static final class EjecutorDirecto implements EjecutorTransaccional {
 
        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }
}
 