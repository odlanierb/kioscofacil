/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
/**
 *
 * @author odlanierbermudez
 */
class BaseDeDatosTest {

    @Test
    void creaLasCuatroTablas() throws Exception {
        try (BaseDeDatos base = BaseDeDatos.abrir("jdbc:sqlite::memory:");
                Statement st = base.conexion().createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name")) {
            List<String> tablas = new ArrayList<>();
            while (rs.next()) {
                tablas.add(rs.getString("name"));
            }
            assertEquals(List.of("linea_venta", "producto", "stock", "venta"), tablas);
        }
    }

    @Test
    void hacerCumplirLasClavesForaneas() throws Exception {
        try (BaseDeDatos base = BaseDeDatos.abrir("jdbc:sqlite::memory:");
                Statement st = base.conexion().createStatement()) {
            assertThrows(SQLException.class, () -> st.executeUpdate(
                    "INSERT INTO stock (codigo, cantidad) VALUES ('999', 1)"));
        }
    }
}