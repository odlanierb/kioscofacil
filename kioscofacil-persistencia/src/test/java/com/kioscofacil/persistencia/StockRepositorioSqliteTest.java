/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class StockRepositorioSqliteTest {

    private BaseDeDatos baseDeDatos;
    private ProductoRepositorioSqlite productos;
    private StockRepositorioSqlite repositorio;

    @BeforeEach
    void abrirBaseDeDatos() {
        baseDeDatos = BaseDeDatos.abrir("jdbc:sqlite::memory:");
        productos = new ProductoRepositorioSqlite(baseDeDatos);
        repositorio = new StockRepositorioSqlite(baseDeDatos);
    }

    @AfterEach
    void cerrarBaseDeDatos() {
        baseDeDatos.close();
    }

    private CodigoBarras registrarProducto(String codigo) {
        CodigoBarras codigoBarras = new CodigoBarras(codigo);
        productos.guardar(new Producto(codigoBarras, "Producto " + codigo, Dinero.de("100")));
        return codigoBarras;
    }

    @Test
    void cantidadDeDevuelveCeroSiNuncaSeCargoStock() {
        CodigoBarras codigo = registrarProducto("7790001");

        assertEquals(0, repositorio.cantidadDe(codigo));
    }

    @Test
    void guardaYRecuperaLaCantidad() {
        CodigoBarras codigo = registrarProducto("7790001");

        repositorio.guardar(codigo, 25);

        assertEquals(25, repositorio.cantidadDe(codigo));
    }

    @Test
    void guardarReemplazaLaCantidadAnterior() {
        CodigoBarras codigo = registrarProducto("7790001");
        repositorio.guardar(codigo, 25);

        repositorio.guardar(codigo, 10);

        assertEquals(10, repositorio.cantidadDe(codigo));
    }

    @Test
    void aceptaCantidadesNegativas() {
        CodigoBarras codigo = registrarProducto("7790001");

        repositorio.guardar(codigo, -3);

        assertEquals(-3, repositorio.cantidadDe(codigo));
    }

    @Test
    void guardaElStockDeCadaProductoPorSeparado() {
        CodigoBarras alfajor = registrarProducto("7790001");
        CodigoBarras chicle = registrarProducto("7790002");

        repositorio.guardar(alfajor, 5);
        repositorio.guardar(chicle, 40);

        assertEquals(5, repositorio.cantidadDe(alfajor));
        assertEquals(40, repositorio.cantidadDe(chicle));
    }

    @Test
    void noPermiteGuardarStockDeUnProductoInexistente() {
        CodigoBarras inexistente = new CodigoBarras("999");

        assertThrows(PersistenciaException.class, () -> repositorio.guardar(inexistente, 5));
    }
}