/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;


/**
 *
 * @author odlanierbermudez
 */
public final class RespaldoSqlite {

    private static final String RESPALDAR = "VACUUM INTO ?";

    private final BaseDeDatos baseDeDatos;

    public RespaldoSqlite(BaseDeDatos baseDeDatos) {
        this.baseDeDatos = Objects.requireNonNull(baseDeDatos);
    }

    /**
     * Crea en archivoDestino una copia consistente de la base de datos.
     * Falla si la carpeta no existe o si el archivo ya existe: nunca pisa un respaldo.
     */
    public void respaldarEn(Path archivoDestino) {
        Objects.requireNonNull(archivoDestino);
        try (PreparedStatement sentencia = baseDeDatos.conexion().prepareStatement(RESPALDAR)) {
            sentencia.setString(1, archivoDestino.toAbsolutePath().toString());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo crear el respaldo en " + archivoDestino, e);
        }
    }
}