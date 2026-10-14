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
import com.kioscofacil.core.venta.Cobro;
import com.kioscofacil.core.venta.LineaVenta;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.VentaRealizada;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class VentaRepositorioSqliteTest {

    private static final LocalDateTime MEDIODIA = LocalDateTime.of(2026, 10, 13, 12, 0, 0);

    private BaseDeDatos baseDeDatos;
    private VentaRepositorioSqlite repositorio;

    @BeforeEach
    void abrirBaseDeDatos() {
        baseDeDatos = BaseDeDatos.abrir("jdbc:sqlite::memory:");
        repositorio = new VentaRepositorioSqlite(baseDeDatos);
    }

    @AfterEach
    void cerrarBaseDeDatos() {
        baseDeDatos.close();
    }

    private static LineaVenta linea(String codigo, String nombre, String precio, int cantidad) {
        return new LineaVenta(new CodigoBarras(codigo), nombre, Dinero.de(precio), cantidad);
    }

    private static VentaRealizada ventaExacta(
            LocalDateTime fecha, MedioPago medio, LineaVenta... lineas) {
        Dinero total = Dinero.CERO;
        for (LineaVenta linea : lineas) {
            total = total.sumar(linea.subtotal());
        }
        return new VentaRealizada(
                UUID.randomUUID(), fecha, List.of(lineas), new Cobro(medio, total, Dinero.CERO));
    }

    private static VentaRealizada ventaEn(LocalDateTime fecha) {
        return ventaExacta(fecha, MedioPago.EFECTIVO, linea("7790001", "Alfajor", "850", 1));
    }

    @Test
    void guardaYRecuperaUnaVentaConVariasLineasYVuelto() {
        // Los productos no existen en la base: cada línea guarda su propia copia
        // del nombre y del precio.
        VentaRealizada venta = new VentaRealizada(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 10, 13, 18, 45, 30),
                List.of(linea("7790001", "Alfajor", "850.50", 2),
                        linea("7790002", "Chicle", "100", 3)),
                new Cobro(MedioPago.EFECTIVO, Dinero.de("2500"), Dinero.de("499")));

        repositorio.guardar(venta);

        assertEquals(Optional.of(venta), repositorio.buscarPorId(venta.id()));
    }

    @Test
    void conservaElOrdenEnQueSeGuardaronLasLineas() {
        VentaRealizada venta = ventaExacta(MEDIODIA, MedioPago.EFECTIVO,
                linea("300", "Gaseosa", "10", 1),
                linea("100", "Alfajor", "20", 1),
                linea("200", "Chicle", "30", 1));

        repositorio.guardar(venta);

        VentaRealizada recuperada = repositorio.buscarPorId(venta.id()).orElseThrow();
        assertEquals(venta.lineas(), recuperada.lineas());
    }

    @Test
    void conservaElMedioDePago() {
        VentaRealizada venta = ventaExacta(MEDIODIA, MedioPago.BILLETERA_VIRTUAL,
                linea("7790001", "Alfajor", "850", 1));

        repositorio.guardar(venta);

        MedioPago medio = repositorio.buscarPorId(venta.id()).orElseThrow().cobro().medio();
        assertEquals(MedioPago.BILLETERA_VIRTUAL, medio);
    }

    @Test
    void buscarPorIdDevuelveVacioSiNoExiste() {
        assertEquals(Optional.empty(), repositorio.buscarPorId(UUID.randomUUID()));
    }

    @Test
    void noPermiteGuardarDosVecesLaMismaVenta() {
        VentaRealizada venta = ventaEn(MEDIODIA);
        repositorio.guardar(venta);

        assertThrows(PersistenciaException.class, () -> repositorio.guardar(venta));
    }

    @Test
    void listarEntreIncluyeLaFechaDesdeYExcluyeLaFechaHasta() {
        VentaRealizada anterior = ventaEn(LocalDateTime.of(2026, 10, 12, 23, 59, 59));
        VentaRealizada alInicio = ventaEn(LocalDateTime.of(2026, 10, 13, 0, 0, 0));
        VentaRealizada alFinal = ventaEn(LocalDateTime.of(2026, 10, 13, 23, 59, 59));
        VentaRealizada alDiaSiguiente = ventaEn(LocalDateTime.of(2026, 10, 14, 0, 0, 0));
        repositorio.guardar(anterior);
        repositorio.guardar(alInicio);
        repositorio.guardar(alFinal);
        repositorio.guardar(alDiaSiguiente);

        List<VentaRealizada> ventas = repositorio.listarEntre(
                LocalDateTime.of(2026, 10, 13, 0, 0, 0),
                LocalDateTime.of(2026, 10, 14, 0, 0, 0));

        assertEquals(List.of(alInicio, alFinal), ventas);
    }

    @Test
    void listarEntreOrdenaLasVentasPorFechaAscendente() {
        VentaRealizada tarde = ventaEn(LocalDateTime.of(2026, 10, 13, 18, 0, 0));
        VentaRealizada manana = ventaEn(LocalDateTime.of(2026, 10, 13, 9, 0, 0));
        VentaRealizada mediodia = ventaEn(MEDIODIA);
        repositorio.guardar(tarde);
        repositorio.guardar(manana);
        repositorio.guardar(mediodia);

        List<VentaRealizada> ventas = repositorio.listarEntre(
                LocalDateTime.of(2026, 10, 13, 0, 0, 0),
                LocalDateTime.of(2026, 10, 14, 0, 0, 0));

        assertEquals(List.of(manana, mediodia, tarde), ventas);
    }

    @Test
    void listarEntreDevuelveListaVaciaSinVentasEnElRango() {
        repositorio.guardar(ventaEn(MEDIODIA));

        List<VentaRealizada> ventas = repositorio.listarEntre(
                LocalDateTime.of(2026, 11, 1, 0, 0, 0),
                LocalDateTime.of(2026, 11, 2, 0, 0, 0));

        assertTrue(ventas.isEmpty());
    }

    @Test
    void fallaConPersistenciaExceptionSiLaVentaGuardadaNoTieneLineas() throws SQLException {
        UUID id = UUID.randomUUID();
        try (Statement sentencia = baseDeDatos.conexion().createStatement()) {
            sentencia.executeUpdate(
                    "INSERT INTO venta (id, fecha, medio_pago, recibido_centavos, vuelto_centavos) "
                    + "VALUES ('" + id + "', '2026-10-13T10:00:00.000', 'EFECTIVO', 1000, 0)");
        }

        assertThrows(PersistenciaException.class, () -> repositorio.buscarPorId(id));
    }
}