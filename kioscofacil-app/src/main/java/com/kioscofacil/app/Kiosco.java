/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.app;

import com.kioscofacil.core.producto.ProductoServicio;
import com.kioscofacil.core.respaldo.Respaldador;
import com.kioscofacil.core.stock.StockServicio;
import com.kioscofacil.core.venta.VentaServicio;
import com.kioscofacil.persistencia.BaseDeDatos;
import com.kioscofacil.persistencia.EjecutorSqlite;
import com.kioscofacil.persistencia.ProductoRepositorioSqlite;
import com.kioscofacil.persistencia.RespaldoSqlite;
import com.kioscofacil.persistencia.StockRepositorioSqlite;
import com.kioscofacil.persistencia.VentaRepositorioSqlite;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
/**
 *
 * @author odlanierbermudez
 */
public final class Kiosco implements Respaldador, AutoCloseable {

    private static final DateTimeFormatter FORMATO_NOMBRE_RESPALDO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final BaseDeDatos baseDeDatos;
    private final Clock reloj;
    private final ProductoServicio productos;
    private final StockServicio stock;
    private final VentaServicio ventas;
    private final RespaldoSqlite respaldo;

    private Kiosco(BaseDeDatos baseDeDatos, Clock reloj, ProductoServicio productos,
            StockServicio stock, VentaServicio ventas) {
        this.baseDeDatos = baseDeDatos;
        this.reloj = reloj;
        this.productos = productos;
        this.stock = stock;
        this.ventas = ventas;
        this.respaldo = new RespaldoSqlite(baseDeDatos);
    }

    public static Kiosco abrir(Path archivoDeBaseDeDatos, Clock reloj) {
        Objects.requireNonNull(archivoDeBaseDeDatos);
        Objects.requireNonNull(reloj);
        crearCarpeta(archivoDeBaseDeDatos.toAbsolutePath().getParent());

        BaseDeDatos baseDeDatos = BaseDeDatos.abrir("jdbc:sqlite:" + archivoDeBaseDeDatos);
        ProductoServicio productos =
                new ProductoServicio(new ProductoRepositorioSqlite(baseDeDatos));
        StockServicio stock =
                new StockServicio(new StockRepositorioSqlite(baseDeDatos), productos);
        VentaServicio ventas = new VentaServicio(
                new VentaRepositorioSqlite(baseDeDatos),
                reloj,
                stock,
                new EjecutorSqlite(baseDeDatos));
        return new Kiosco(baseDeDatos, reloj, productos, stock, ventas);
    }

    /**
     * Guarda en carpetaDestino una copia de la base de datos, con la fecha y la hora
     * en el nombre del archivo. Devuelve el archivo creado.
     */
    @Override
    public Path respaldar(Path carpetaDestino) {
        Objects.requireNonNull(carpetaDestino);
        crearCarpeta(carpetaDestino);
        String nombre = "kiosco-"
                + FORMATO_NOMBRE_RESPALDO.format(LocalDateTime.now(reloj)) + ".db";
        Path archivo = carpetaDestino.resolve(nombre);
        respaldo.respaldarEn(archivo);
        return archivo;
    }

    private static void crearCarpeta(Path carpeta) {
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta " + carpeta, e);
        }
    }

    public ProductoServicio productos() {
        return productos;
    }

    public StockServicio stock() {
        return stock;
    }

    public VentaServicio ventas() {
        return ventas;
    }

    @Override
    public void close() {
        baseDeDatos.close();
    }
}