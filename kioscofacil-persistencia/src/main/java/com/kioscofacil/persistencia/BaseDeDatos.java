/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author odlanierbermudez
 */
public final class BaseDeDatos implements AutoCloseable {

    private final Connection conexion;

    private BaseDeDatos(Connection conexion) {
        this.conexion = conexion;
    }

    /** Abre (o crea) la base. Ejemplos: "jdbc:sqlite:kiosco.db" o "jdbc:sqlite::memory:". */
    public static BaseDeDatos abrir(String url) {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(url);
            try (Statement st = conexion.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            crearTablas(conexion);
            return new BaseDeDatos(conexion);
        } catch (SQLException | IOException e) {
            cerrarSilenciosamente(conexion);
            throw new PersistenciaException("No se pudo abrir la base de datos", e);
        }
    }

    Connection conexion() {
        return conexion;
    }

    @Override
    public void close() {
        try {
            conexion.close();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo cerrar la base de datos", e);
        }
    }

    private static void crearTablas(Connection conexion) throws SQLException, IOException {
        try (InputStream in = BaseDeDatos.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IOException("No se encontró schema.sql");
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement st = conexion.createStatement()) {
                for (String sentencia : script.split(";")) {
                    if (!sentencia.isBlank()) {
                        st.execute(sentencia);
                    }
                }
            }
        }
    }

    private static void cerrarSilenciosamente(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignorada) {
                // ya estamos manejando otro error
            }
        }
    }
}