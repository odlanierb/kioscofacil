/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.stock.StockRepositorio;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
/**
 *
 * @author odlanierbermudez
 */
public final class StockRepositorioSqlite implements StockRepositorio {

    private static final String CANTIDAD = """
            SELECT cantidad
            FROM stock
            WHERE codigo = ?
            """;

    private static final String GUARDAR = """
            INSERT INTO stock (codigo, cantidad)
            VALUES (?, ?)
            ON CONFLICT (codigo) DO UPDATE SET
                cantidad = excluded.cantidad
            """;

    private final BaseDeDatos baseDeDatos;

    public StockRepositorioSqlite(BaseDeDatos baseDeDatos) {
        this.baseDeDatos = Objects.requireNonNull(baseDeDatos);
    }

    @Override
    public int cantidadDe(CodigoBarras codigo) {
        Objects.requireNonNull(codigo);
        try (PreparedStatement sentencia = baseDeDatos.conexion().prepareStatement(CANTIDAD)) {
            sentencia.setString(1, codigo.valor());
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("cantidad");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo leer el stock del producto " + codigo.valor(), e);
        }
    }

    @Override
    public void guardar(CodigoBarras codigo, int cantidad) {
        Objects.requireNonNull(codigo);
        try (PreparedStatement sentencia = baseDeDatos.conexion().prepareStatement(GUARDAR)) {
            sentencia.setString(1, codigo.valor());
            sentencia.setInt(2, cantidad);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo guardar el stock del producto " + codigo.valor(), e);
        }
    }
}