/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.producto.CodigoBarras;
import com.kioscofacil.core.venta.MedioPago;
import com.kioscofacil.core.venta.Venta;
import com.kioscofacil.core.venta.VentaRealizada;
import com.kioscofacil.persistencia.PersistenciaException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author odlanierbermudez
 */
class KioscoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    // 15:30 UTC es 12:30 en Buenos Aires
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-13T15:30:00Z"), ZONA);
    private static final CodigoBarras CODIGO = new CodigoBarras("7790001");

    @TempDir
    Path carpetaTemporal;

    @Test
    void creaLaCarpetaYElArchivoDeLaBaseSiNoExisten() {
        Path archivo = carpetaTemporal.resolve("datos").resolve("kiosco.db");

        try (Kiosco kiosco = Kiosco.abrir(archivo, RELOJ)) {
            assertTrue(Files.exists(archivo));
        }
    }

    @Test
    void losDatosSobrevivenAlCerrarYVolverAAbrir() {
        Path archivo = carpetaTemporal.resolve("kiosco.db");
        try (Kiosco kiosco = Kiosco.abrir(archivo, RELOJ)) {
            kiosco.productos().registrar(CODIGO, "Alfajor", Dinero.de("850"));
            kiosco.stock().ingresar(CODIGO, 10);
            Venta venta = new Venta();
            venta.agregar(kiosco.productos().buscarPorCodigo(CODIGO), 2);
            venta.cobrar(MedioPago.EFECTIVO, Dinero.de("2000"));
            kiosco.ventas().registrar(venta);
        }

        try (Kiosco kiosco = Kiosco.abrir(archivo, RELOJ)) {
            assertEquals("Alfajor", kiosco.productos().buscarPorCodigo(CODIGO).nombre());
            assertEquals(8, kiosco.stock().cantidadDe(CODIGO));
            assertEquals(1, kiosco.ventas().ventasDelDia(LocalDate.of(2026, 10, 13)).size());
        }
    }

    @Test
    void lasVentasUsanElRelojRecibido() {
        try (Kiosco kiosco = Kiosco.abrir(carpetaTemporal.resolve("kiosco.db"), RELOJ)) {
            kiosco.productos().registrar(CODIGO, "Alfajor", Dinero.de("850"));
            Venta venta = new Venta();
            venta.agregar(kiosco.productos().buscarPorCodigo(CODIGO), 1);
            venta.cobrar(MedioPago.TARJETA, venta.total());

            VentaRealizada realizada = kiosco.ventas().registrar(venta);

            assertEquals(LocalDateTime.of(2026, 10, 13, 12, 30, 0), realizada.fecha());
        }
    }

    @Test
    void abrirFallaSiNoSePuedeCrearLaCarpeta() throws IOException {
        Path ocupado = Files.createFile(carpetaTemporal.resolve("ocupado"));

        assertThrows(UncheckedIOException.class,
                () -> Kiosco.abrir(ocupado.resolve("kiosco.db"), RELOJ));
    }

    @Test
    void respaldarCreaLaCarpetaYUnArchivoConLaFechaYHoraEnElNombre() {
        Path carpetaDeRespaldos = carpetaTemporal.resolve("respaldos");

        try (Kiosco kiosco = Kiosco.abrir(carpetaTemporal.resolve("kiosco.db"), RELOJ)) {
            Path respaldo = kiosco.respaldar(carpetaDeRespaldos);

            assertEquals("kiosco-2026-10-13_12-30-00.db", respaldo.getFileName().toString());
            assertEquals(carpetaDeRespaldos, respaldo.getParent());
            assertTrue(Files.exists(respaldo));
        }
    }

    @Test
    void elRespaldoSePuedeAbrirComoUnKioscoConTodosSusDatos() {
        Path respaldo;
        try (Kiosco kiosco = Kiosco.abrir(carpetaTemporal.resolve("kiosco.db"), RELOJ)) {
            kiosco.productos().registrar(CODIGO, "Alfajor", Dinero.de("850"));
            kiosco.stock().ingresar(CODIGO, 10);
            respaldo = kiosco.respaldar(carpetaTemporal.resolve("respaldos"));
        }

        try (Kiosco copia = Kiosco.abrir(respaldo, RELOJ)) {
            assertEquals("Alfajor", copia.productos().buscarPorCodigo(CODIGO).nombre());
            assertEquals(10, copia.stock().cantidadDe(CODIGO));
        }
    }

    @Test
    void dosRespaldosEnElMismoSegundoNoSePisan() {
        Path carpeta = carpetaTemporal.resolve("respaldos");

        try (Kiosco kiosco = Kiosco.abrir(carpetaTemporal.resolve("kiosco.db"), RELOJ)) {
            kiosco.respaldar(carpeta);

            assertThrows(PersistenciaException.class, () -> kiosco.respaldar(carpeta));
        }
    }

    @Test
    void respaldarFallaSiNoSePuedeCrearLaCarpeta() throws IOException {
        Path ocupado = Files.createFile(carpetaTemporal.resolve("ocupado"));

        try (Kiosco kiosco = Kiosco.abrir(carpetaTemporal.resolve("kiosco.db"), RELOJ)) {
            assertThrows(UncheckedIOException.class,
                    () -> kiosco.respaldar(ocupado.resolve("respaldos")));
        }
    }
}