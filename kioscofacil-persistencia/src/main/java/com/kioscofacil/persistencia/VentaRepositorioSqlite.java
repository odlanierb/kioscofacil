/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.persistencia;

import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.venta.Cobro;
import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.core.venta.VentaRepositorio;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author odlanierbermudez
 */
public final class VentaRepositorioSqlite implements VentaRepositorio {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private static final String INSERTAR_VENTA = """
            INSERT INTO venta (id, fecha, medio_pago, recibido_centavos, vuelto_centavos)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String INSERTAR_LINEA = """
            INSERT INTO linea_venta (venta_id, codigo, nombre, precio_unitario_centavos, cantidad)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String BUSCAR_POR_ID = """
            SELECT id, fecha, medio_pago, recibido_centavos, vuelto_centavos
            FROM venta
            WHERE id = ?
            """;

    private static final String LISTAR_ENTRE = """
            SELECT id, fecha, medio_pago, recibido_centavos, vuelto_centavos
            FROM venta
            WHERE fecha >= ? AND fecha < ?
            ORDER BY fecha, id
            """;

    private static final String LINEAS_DE_VENTA = """
            SELECT codigo, nombre, precio_unitario_centavos, cantidad
            FROM linea_venta
            WHERE venta_id = ?
            ORDER BY rowid
            """;

    private record Cabecera(UUID id, LocalDateTime fecha, Cobro cobro) {
    }

    private final BaseDeDatos baseDeDatos;

    public VentaRepositorioSqlite(BaseDeDatos baseDeDatos) {
        this.baseDeDatos = Objects.requireNonNull(baseDeDatos);
    }

    @Override
    public void guardar(VentaRealizada venta) {
        Objects.requireNonNull(venta);
        String id = venta.id().toString();
        try (PreparedStatement sentenciaVenta =
                     baseDeDatos.conexion().prepareStatement(INSERTAR_VENTA);
             PreparedStatement sentenciaLinea =
                     baseDeDatos.conexion().prepareStatement(INSERTAR_LINEA)) {
            Cobro cobro = venta.cobro();
            sentenciaVenta.setString(1, id);
            sentenciaVenta.setString(2, venta.fecha().format(FORMATO_FECHA));
            sentenciaVenta.setString(3, cobro.medio().name());
            sentenciaVenta.setLong(4, ConversorDinero.aCentavos(cobro.recibido()));
            sentenciaVenta.setLong(5, ConversorDinero.aCentavos(cobro.vuelto()));
            sentenciaVenta.executeUpdate();

            for (LineaVenta linea : venta.lineas()) {
                sentenciaLinea.setString(1, id);
                sentenciaLinea.setString(2, linea.codigo().valor());
                sentenciaLinea.setString(3, linea.nombre());
                sentenciaLinea.setLong(4, ConversorDinero.aCentavos(linea.precioUnitario()));
                sentenciaLinea.setInt(5, linea.cantidad());
                sentenciaLinea.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la venta " + id, e);
        }
    }

    @Override
    public Optional<VentaRealizada> buscarPorId(UUID id) {
        Objects.requireNonNull(id);
        try (PreparedStatement sentencia =
                     baseDeDatos.conexion().prepareStatement(BUSCAR_POR_ID)) {
            sentencia.setString(1, id.toString());
            List<Cabecera> cabeceras = leerCabeceras(sentencia);
            if (cabeceras.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(armarVenta(cabeceras.get(0)));
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar la venta " + id, e);
        }
    }

    @Override
    public List<VentaRealizada> listarEntre(LocalDateTime desde, LocalDateTime hasta) {
        Objects.requireNonNull(desde);
        Objects.requireNonNull(hasta);
        try (PreparedStatement sentencia =
                     baseDeDatos.conexion().prepareStatement(LISTAR_ENTRE)) {
            sentencia.setString(1, desde.format(FORMATO_FECHA));
            sentencia.setString(2, hasta.format(FORMATO_FECHA));
            List<VentaRealizada> ventas = new ArrayList<>();
            for (Cabecera cabecera : leerCabeceras(sentencia)) {
                ventas.add(armarVenta(cabecera));
            }
            return ventas;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar las ventas", e);
        }
    }

    private static List<Cabecera> leerCabeceras(PreparedStatement sentencia)
            throws SQLException {
        List<Cabecera> cabeceras = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                cabeceras.add(aCabecera(resultado));
            }
        }
        return cabeceras;
    }

    private static Cabecera aCabecera(ResultSet resultado) throws SQLException {
        String id = resultado.getString("id");
        try {
            Cobro cobro = new Cobro(
                    MedioPago.valueOf(resultado.getString("medio_pago")),
                    ConversorDinero.deCentavos(resultado.getLong("recibido_centavos")),
                    ConversorDinero.deCentavos(resultado.getLong("vuelto_centavos")));
            return new Cabecera(
                    UUID.fromString(id),
                    LocalDateTime.parse(resultado.getString("fecha"), FORMATO_FECHA),
                    cobro);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new PersistenciaException("Datos inválidos en la base para la venta " + id, e);
        }
    }

    private VentaRealizada armarVenta(Cabecera cabecera) throws SQLException {
        List<LineaVenta> lineas = leerLineas(cabecera.id());
        try {
            return new VentaRealizada(cabecera.id(), cabecera.fecha(), lineas, cabecera.cobro());
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException(
                    "Datos inválidos en la base para la venta " + cabecera.id(), e);
        }
    }

    private List<LineaVenta> leerLineas(UUID ventaId) throws SQLException {
        List<LineaVenta> lineas = new ArrayList<>();
        try (PreparedStatement sentencia =
                     baseDeDatos.conexion().prepareStatement(LINEAS_DE_VENTA)) {
            sentencia.setString(1, ventaId.toString());
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lineas.add(aLinea(resultado, ventaId));
                }
            }
        }
        return lineas;
    }

    private static LineaVenta aLinea(ResultSet resultado, UUID ventaId) throws SQLException {
        try {
            return new LineaVenta(
                    new CodigoBarras(resultado.getString("codigo")),
                    resultado.getString("nombre"),
                    ConversorDinero.deCentavos(resultado.getLong("precio_unitario_centavos")),
                    resultado.getInt("cantidad"));
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException(
                    "Datos inválidos en una línea de la venta " + ventaId, e);
        }
    }
}