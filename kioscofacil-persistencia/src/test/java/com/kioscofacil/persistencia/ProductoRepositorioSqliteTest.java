/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 *
 * @author odlanierbermudez
 */
class ProductoRepositorioSqliteTest {

    private BaseDeDatos baseDeDatos;
    private ProductoRepositorioSqlite repositorio;

    @BeforeEach
    void abrirBaseDeDatos() {
        baseDeDatos = BaseDeDatos.abrir("jdbc:sqlite::memory:");
        repositorio = new ProductoRepositorioSqlite(baseDeDatos);
    }

    @AfterEach
    void cerrarBaseDeDatos() {
        baseDeDatos.close();
    }

    private static Producto producto(String codigo, String nombre, String precio) {
        return new Producto(new CodigoBarras(codigo), nombre, Dinero.de(precio));
    }

    @Test
    void guardaYRecuperaUnProducto() {
        Producto alfajor = producto("7790001", "Alfajor", "850.50");

        repositorio.guardar(alfajor);

        assertEquals(Optional.of(alfajor), repositorio.buscarPorCodigo(alfajor.codigo()));
    }

    @Test
    void buscarPorCodigoDevuelveVacioSiNoExiste() {
        assertEquals(Optional.empty(), repositorio.buscarPorCodigo(new CodigoBarras("999")));
    }

    @Test
    void guardarReemplazaElProductoConElMismoCodigo() {
        Producto original = producto("7790001", "Alfajor", "850.50");
        repositorio.guardar(original);

        Producto conOtroPrecio = original.conPrecio(Dinero.de("900"));
        repositorio.guardar(conOtroPrecio);

        assertEquals(Optional.of(conOtroPrecio), repositorio.buscarPorCodigo(original.codigo()));
        assertEquals(1, repositorio.listarTodos().size());
    }

    @Test
    void conservaLosCerosALaIzquierdaDelCodigo() {
        Producto producto = producto("0012345", "Chicle", "100");

        repositorio.guardar(producto);

        Optional<Producto> encontrado = repositorio.buscarPorCodigo(new CodigoBarras("0012345"));
        assertEquals("0012345", encontrado.orElseThrow().codigo().valor());
    }

    @Test
    void listarTodosDevuelveListaVaciaSinProductos() {
        assertTrue(repositorio.listarTodos().isEmpty());
    }

    @Test
    void listarTodosOrdenaPorNombreSinImportarMayusculas() {
        Producto coca = producto("1", "Coca Cola", "2000");
        Producto alfajor = producto("2", "alfajor", "850");
        Producto bizcocho = producto("3", "Bizcocho", "500");
        repositorio.guardar(coca);
        repositorio.guardar(alfajor);
        repositorio.guardar(bizcocho);

        assertEquals(List.of(alfajor, bizcocho, coca), repositorio.listarTodos());
    }

    @Test
    void fallaConPersistenciaExceptionSiLaBaseTieneDatosInvalidos() throws SQLException {
        try (Statement sentencia = baseDeDatos.conexion().createStatement()) {
            sentencia.executeUpdate(
                    "INSERT INTO producto (codigo, nombre, precio_centavos) VALUES ('111', '   ', 100)");
        }

        assertThrows(PersistenciaException.class,
                () -> repositorio.buscarPorCodigo(new CodigoBarras("111")));
    }
}
