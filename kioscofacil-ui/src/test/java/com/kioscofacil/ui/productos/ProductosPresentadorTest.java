/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.producto.ProductoRepositorio;
import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.stock.StockRepositorio;
import com.kioscofacil.core.stock.StockServicio;
import com.kioscofacil.ui.productos.ProductosPresentador.Fila;
import com.kioscofacil.ui.productos.ProductosPresentador.Resultado;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class ProductosPresentadorTest {
 
    private static final String CODIGO = "7790001";
 
    private ProductosEnMemoria repositorioDeProductos;
    private StockEnMemoria repositorioDeStock;
    private ProductosPresentador presentador;
 
    @BeforeEach
    void preparar() {
        repositorioDeProductos = new ProductosEnMemoria();
        repositorioDeStock = new StockEnMemoria();
        ProductoServicio productos = new ProductoServicio(repositorioDeProductos);
        StockServicio stock = new StockServicio(repositorioDeStock, productos);
        presentador = new ProductosPresentador(productos, stock);
    }
 
    @Test
    void registrarAgregaElProductoConSuStockInicial() {
        Resultado resultado = presentador.registrar(CODIGO, "Alfajor", "1500,50", "12");
 
        assertTrue(resultado.exito());
        assertTrue(resultado.mensaje().contains("Alfajor"));
        List<Fila> filas = presentador.filas();
        assertEquals(1, filas.size());
        assertEquals(Dinero.de("1500.50"), filas.get(0).producto().precio());
        assertEquals(12, filas.get(0).stock());
    }
 
    @Test
    void registrarSinStockInicialDejaElStockEnCero() {
        Resultado resultado = presentador.registrar(CODIGO, "Alfajor", "1500", "");
 
        assertTrue(resultado.exito());
        assertEquals(0, presentador.filas().get(0).stock());
    }
 
    @Test
    void registrarConUnCodigoRepetidoNoPisaElProductoYDiceCualYaExiste() {
        presentador.registrar(CODIGO, "Alfajor", "1500", "");
 
        Resultado resultado = presentador.registrar(CODIGO, "Otro nombre", "99", "");
 
        assertFalse(resultado.exito());
        assertTrue(resultado.mensaje().contains("Alfajor"));
        assertEquals(1, presentador.filas().size());
        assertEquals(Dinero.de("1500"), presentador.filas().get(0).producto().precio());
    }
 
    @Test
    void unPrecioInvalidoNoRegistraNada() {
        for (String precio : List.of("abc", "", "   ", "1.500", "-5", "10,999")) {
            Resultado resultado = presentador.registrar(CODIGO, "Alfajor", precio, "");
 
            assertFalse(resultado.exito(), "Debió rechazar el precio: " + precio);
        }
        assertTrue(presentador.filas().isEmpty());
    }
 
    @Test
    void elPrecioAceptaPuntoComaYSignoPesos() {
        assertEquals(Dinero.de("1500.50"), ProductosPresentador.leerPrecio("1500,50"));
        assertEquals(Dinero.de("1500.50"), ProductosPresentador.leerPrecio("1500.50"));
        assertEquals(Dinero.de("800"), ProductosPresentador.leerPrecio(" $ 800 "));
    }
 
    @Test
    void unaCantidadInvalidaNoRegistraNada() {
        Resultado resultado = presentador.registrar(CODIGO, "Alfajor", "1500", "doce");
 
        assertFalse(resultado.exito());
        assertTrue(presentador.filas().isEmpty());
    }
 
    @Test
    void sinNombreNoRegistraNada() {
        Resultado resultado = presentador.registrar(CODIGO, "   ", "1500", "");
 
        assertFalse(resultado.exito());
        assertTrue(presentador.filas().isEmpty());
    }
 
    @Test
    void cambiarPrecioActualizaElProducto() {
        presentador.registrar(CODIGO, "Alfajor", "1500", "");
 
        Resultado resultado = presentador.cambiarPrecio(CODIGO, "1700");
 
        assertTrue(resultado.exito());
        assertEquals(Dinero.de("1700"), presentador.filas().get(0).producto().precio());
    }
 
    @Test
    void cambiarElPrecioDeUnProductoInexistenteExplicaQueNoExiste() {
        Resultado resultado = presentador.cambiarPrecio(CODIGO, "1700");
 
        assertFalse(resultado.exito());
        assertTrue(resultado.mensaje().contains(CODIGO));
    }
 
    @Test
    void sumarStockSeSumaAlStockActual() {
        presentador.registrar(CODIGO, "Alfajor", "1500", "5");
 
        Resultado resultado = presentador.sumarStock(CODIGO, "10");
 
        assertTrue(resultado.exito());
        assertTrue(resultado.mensaje().contains("15"));
        assertEquals(15, presentador.filas().get(0).stock());
    }
 
    @Test
    void sumarCeroUnidadesSeRechazaYNoCambiaElStock() {
        presentador.registrar(CODIGO, "Alfajor", "1500", "5");
 
        Resultado resultado = presentador.sumarStock(CODIGO, "0");
 
        assertFalse(resultado.exito());
        assertEquals(5, presentador.filas().get(0).stock());
    }
 
    @Test
    void sumarStockDeUnProductoInexistenteNoCreaStock() {
        Resultado resultado = presentador.sumarStock(CODIGO, "10");
 
        assertFalse(resultado.exito());
        assertEquals(0, repositorioDeStock.cantidadDe(new CodigoBarras(CODIGO)));
    }
 
    @Test
    void fijarStockDejaLaCantidadContada() {
        presentador.registrar(CODIGO, "Alfajor", "1500", "10");
 
        Resultado resultado = presentador.fijarStock(CODIGO, "3");
 
        assertTrue(resultado.exito());
        assertEquals(3, presentador.filas().get(0).stock());
    }
 
    @Test
    void sinCodigoLasAccionesDanUnError() {
        assertFalse(presentador.sumarStock("  ", "10").exito());
        assertFalse(presentador.fijarStock("", "10").exito());
        assertFalse(presentador.cambiarPrecio("", "10").exito());
    }
 
    @Test
    void elPrecioSeMuestraConFormatoArgentino() {
        assertEquals("$ 1.500,50", ProductosPresentador.formatearPrecio(Dinero.de("1500.5")));
        assertEquals("$ 0,00", ProductosPresentador.formatearPrecio(Dinero.CERO));
    }
 
    @Test
    void elPrecioParaEditarUsaComaDecimal() {
        assertEquals("1500,50", ProductosPresentador.precioParaEditar(Dinero.de("1500.5")));
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
}
 