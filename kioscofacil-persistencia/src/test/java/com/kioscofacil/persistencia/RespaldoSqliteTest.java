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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author odlanierbermudez
 */
class RespaldoSqliteTest {

    @TempDir
    Path carpetaTemporal;

    private BaseDeDatos baseDeDatos;
    private ProductoRepositorioSqlite productos;
    private StockRepositorioSqlite stock;
    private RespaldoSqlite respaldo;

    @BeforeEach
    void abrirBaseDeDatos() {
        baseDeDatos = BaseDeDatos.abrir("jdbc:sqlite::memory:");
        productos = new ProductoRepositorioSqlite(baseDeDatos);
        stock = new StockRepositorioSqlite(baseDeDatos);
        respaldo = new RespaldoSqlite(baseDeDatos);
    }

    @AfterEach
    void cerrarBaseDeDatos() {
        baseDeDatos.close();
    }

    private static Producto alfajor() {
        return new Producto(new CodigoBarras("7790001"), "Alfajor", Dinero.de("850"));
    }

    private static Producto chicle() {
        return new Producto(new CodigoBarras("7790002"), "Chicle", Dinero.de("100"));
    }

    @Test
    void elRespaldoContieneLosDatosDeLaBase() {
        Producto alfajor = alfajor();
        productos.guardar(alfajor);
        stock.guardar(alfajor.codigo(), 10);
        Path destino = carpetaTemporal.resolve("copia.db");

        respaldo.respaldarEn(destino);

        try (BaseDeDatos copia = BaseDeDatos.abrir("jdbc:sqlite:" + destino)) {
            assertEquals(Optional.of(alfajor),
                    new ProductoRepositorioSqlite(copia).buscarPorCodigo(alfajor.codigo()));
            assertEquals(10, new StockRepositorioSqlite(copia).cantidadDe(alfajor.codigo()));
        }
    }

    @Test
    void elRespaldoEsUnaFotoDelMomentoYNoSigueLosCambiosPosteriores() {
        productos.guardar(alfajor());
        Path destino = carpetaTemporal.resolve("copia.db");

        respaldo.respaldarEn(destino);
        productos.guardar(chicle());

        try (BaseDeDatos copia = BaseDeDatos.abrir("jdbc:sqlite:" + destino)) {
            assertEquals(List.of(alfajor()), new ProductoRepositorioSqlite(copia).listarTodos());
        }
        assertEquals(2, productos.listarTodos().size());
    }

    @Test
    void noPisaUnRespaldoQueYaExiste() {
        productos.guardar(alfajor());
        Path destino = carpetaTemporal.resolve("copia.db");
        respaldo.respaldarEn(destino);
        productos.guardar(chicle());

        assertThrows(PersistenciaException.class, () -> respaldo.respaldarEn(destino));

        try (BaseDeDatos copia = BaseDeDatos.abrir("jdbc:sqlite:" + destino)) {
            assertEquals(List.of(alfajor()), new ProductoRepositorioSqlite(copia).listarTodos());
        }
    }

    @Test
    void fallaSiLaCarpetaDeDestinoNoExiste() {
        Path destino = carpetaTemporal.resolve("no-existe").resolve("copia.db");

        assertThrows(PersistenciaException.class, () -> respaldo.respaldarEn(destino));
    }
}