/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;


import com.kioscofacil.core.compartido.EjecutorTransaccional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author odlanierbermudez
 */
public final class EjecutorSqlite implements EjecutorTransaccional {

    private final BaseDeDatos baseDeDatos;

    public EjecutorSqlite(BaseDeDatos baseDeDatos) {
        this.baseDeDatos = Objects.requireNonNull(baseDeDatos);
    }

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        Objects.requireNonNull(operacion);
        Connection conexion = baseDeDatos.conexion();
        if (hayTransaccionEnCurso(conexion)) {
            return operacion.get();
        }
        return ejecutarEnTransaccionNueva(conexion, operacion);
    }

    private static boolean hayTransaccionEnCurso(Connection conexion) {
        try {
            return !conexion.getAutoCommit();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar el estado de la transacción", e);
        }
    }

    private static <T> T ejecutarEnTransaccionNueva(Connection conexion, Supplier<T> operacion) {
        try {
            conexion.setAutoCommit(false);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo iniciar la transacción", e);
        }
        try {
            T resultado = operacion.get();
            conexion.commit();
            return resultado;
        } catch (RuntimeException e) {
            deshacer(conexion, e);
            throw e;
        } catch (SQLException e) {
            deshacer(conexion, e);
            throw new PersistenciaException("No se pudo confirmar la transacción", e);
        } finally {
            restaurarAutoCommit(conexion);
        }
    }

    private static void deshacer(Connection conexion, Exception causa) {
        try {
            conexion.rollback();
        } catch (SQLException e) {
            causa.addSuppressed(e);
        }
    }

    private static void restaurarAutoCommit(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo restaurar el autocommit", e);
        }
    }
}