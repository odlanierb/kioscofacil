/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.producto.Producto;
import com.kioscofacil.core.producto.ProductoRepositorio;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author odlanierbermudez
 */
public final class ProductoRepositorioSqlite implements ProductoRepositorio {

    private static final String GUARDAR = """
            INSERT INTO producto (codigo, nombre, precio_centavos)
            VALUES (?, ?, ?)
            ON CONFLICT (codigo) DO UPDATE SET
                nombre = excluded.nombre,
                precio_centavos = excluded.precio_centavos
            """;

    private static final String BUSCAR = """
            SELECT codigo, nombre, precio_centavos
            FROM producto
            WHERE codigo = ?
            """;

    private static final String LISTAR = """
            SELECT codigo, nombre, precio_centavos
            FROM producto
            ORDER BY nombre COLLATE NOCASE, codigo
            """;

    private final BaseDeDatos baseDeDatos;

    public ProductoRepositorioSqlite(BaseDeDatos baseDeDatos) {
        this.baseDeDatos = Objects.requireNonNull(baseDeDatos);
    }

    @Override
    public void guardar(Producto producto) {
        Objects.requireNonNull(producto);
        try (PreparedStatement sentencia = baseDeDatos.conexion().prepareStatement(GUARDAR)) {
            sentencia.setString(1, producto.codigo().valor());
            sentencia.setString(2, producto.nombre());
            sentencia.setLong(3, ConversorDinero.aCentavos(producto.precio()));
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo guardar el producto " + producto.codigo().valor(), e);
        }
    }

    @Override
    public Optional<Producto> buscarPorCodigo(CodigoBarras codigo) {
        Objects.requireNonNull(codigo);
        try (PreparedStatement sentencia = baseDeDatos.conexion().prepareStatement(BUSCAR)) {
            sentencia.setString(1, codigo.valor());
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(aProducto(resultado));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo buscar el producto " + codigo.valor(), e);
        }
    }

    @Override
    public List<Producto> listarTodos() {
        List<Producto> productos = new ArrayList<>();
        try (PreparedStatement sentencia = baseDeDatos.conexion().prepareStatement(LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                productos.add(aProducto(resultado));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar los productos", e);
        }
        return productos;
    }

    private static Producto aProducto(ResultSet resultado) throws SQLException {
        String codigo = resultado.getString("codigo");
        try {
            return new Producto(
                    new CodigoBarras(codigo),
                    resultado.getString("nombre"),
                    ConversorDinero.deCentavos(resultado.getLong("precio_centavos")));
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException(
                    "Datos inválidos en la base para el producto " + codigo, e);
        }
    }
}