/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author odlanierbermudez
 */
class RespaldoAutomaticoTest {
 
    private static final LocalDate HOY = LocalDate.of(2026, 10, 16);
 
    private final Clock reloj = Clock.fixed(Instant.parse("2026-10-16T15:00:00Z"),
            ZoneId.of("America/Argentina/Buenos_Aires"));
 
    @TempDir
    Path temporal;
 
    private Path carpeta;
 
    @BeforeEach
    void preparar() {
        carpeta = temporal.resolve("Respaldos"); // todavía no existe
    }
 
    @Test
    void guardaLaCopiaDeHoyConElNombreDelDia() throws IOException {
        Path archivo = respaldo(escribiendo("datos")).ejecutar();
 
        assertEquals(carpeta.resolve("kiosco-auto-2026-10-16.db"), archivo);
        assertEquals("datos", Files.readString(archivo));
    }
 
    @Test
    void creaLaCarpetaSiNoExiste() throws IOException {
        respaldo(escribiendo("datos")).ejecutar();
 
        assertTrue(Files.isDirectory(carpeta));
    }
 
    @Test
    void siYaHabiaUnaDeHoyLaReemplazaPorLaNuevaYNoDejaTemporales() throws IOException {
        respaldo(escribiendo("viejo")).ejecutar();
 
        Path archivo = respaldo(escribiendo("nuevo")).ejecutar();
 
        assertEquals("nuevo", Files.readString(archivo));
        assertEquals(List.of("kiosco-auto-2026-10-16.db"), nombresEnLaCarpeta());
    }
 
    @Test
    void siFallaLaCopiaConservaLaDeHoyAnteriorYNoDejaTemporales() throws IOException {
        respaldo(escribiendo("viejo")).ejecutar();
        RespaldoAutomatico.Copiador quePuedeFallar = archivo -> {
            escribir(archivo, "a medias");
            throw new IllegalStateException("disco lleno");
        };
 
        assertThrows(IllegalStateException.class, () -> respaldo(quePuedeFallar).ejecutar());
 
        assertEquals("viejo", Files.readString(carpeta.resolve("kiosco-auto-2026-10-16.db")));
        assertEquals(List.of("kiosco-auto-2026-10-16.db"), nombresEnLaCarpeta());
    }
 
    @Test
    void conservaSoloLasUltimasCopiasAutomaticas() throws IOException {
        for (int dia = 10; dia <= 14; dia++) {
            crearCopia(LocalDate.of(2026, 10, dia));
        }
 
        new RespaldoAutomatico(carpeta, reloj, escribiendo("hoy"), 3).ejecutar();
 
        assertEquals(List.of("kiosco-auto-2026-10-13.db", "kiosco-auto-2026-10-14.db",
                "kiosco-auto-2026-10-16.db"), nombresEnLaCarpeta());
    }
 
    @Test
    void porDefectoConservaLasUltimas30() throws IOException {
        for (int atras = 1; atras <= 35; atras++) {
            crearCopia(HOY.minusDays(atras));
        }
 
        respaldo(escribiendo("hoy")).ejecutar();
 
        assertEquals(30, nombresEnLaCarpeta().size());
        assertTrue(Files.exists(carpeta.resolve(RespaldoAutomatico.nombreDe(HOY))));
        assertTrue(Files.exists(carpeta.resolve(RespaldoAutomatico.nombreDe(HOY.minusDays(29)))));
        assertFalse(Files.exists(carpeta.resolve(RespaldoAutomatico.nombreDe(HOY.minusDays(30)))));
    }
 
    @Test
    void nuncaBorraLosRespaldosManualesNiOtrosArchivos() throws IOException {
        Files.createDirectories(carpeta);
        List<String> ajenos = List.of("kiosco-2026-10-01_10-00-00.db", "notas.txt",
                "kiosco-auto-basura.db", "kiosco-auto-2026-13-45.db");
        for (String nombre : ajenos) {
            Files.writeString(carpeta.resolve(nombre), "no tocar");
        }
        for (int dia = 1; dia <= 5; dia++) {
            crearCopia(LocalDate.of(2026, 10, dia));
        }
 
        new RespaldoAutomatico(carpeta, reloj, escribiendo("hoy"), 1).ejecutar();
 
        for (String nombre : ajenos) {
            assertTrue(Files.exists(carpeta.resolve(nombre)), "No debió borrar " + nombre);
        }
        assertFalse(Files.exists(carpeta.resolve("kiosco-auto-2026-10-05.db")));
        assertTrue(Files.exists(carpeta.resolve("kiosco-auto-2026-10-16.db")));
    }
 
    @Test
    void laCopiaDeHoyNuncaSeBorraAunqueHayaOtrasConFechaFutura() throws IOException {
        for (int dia = 20; dia <= 22; dia++) {
            crearCopia(LocalDate.of(2026, 10, dia));
        }
 
        new RespaldoAutomatico(carpeta, reloj, escribiendo("hoy"), 2).ejecutar();
 
        assertTrue(Files.exists(carpeta.resolve("kiosco-auto-2026-10-16.db")));
        assertFalse(Files.exists(carpeta.resolve("kiosco-auto-2026-10-20.db")));
    }
 
    @Test
    void noAceptaConservarMenosDeUnaCopia() {
        assertThrows(IllegalArgumentException.class,
                () -> new RespaldoAutomatico(carpeta, reloj, escribiendo("x"), 0));
    }
 
    private RespaldoAutomatico respaldo(RespaldoAutomatico.Copiador copiador) {
        return new RespaldoAutomatico(carpeta, reloj, copiador);
    }
 
    private static RespaldoAutomatico.Copiador escribiendo(String contenido) {
        return archivo -> escribir(archivo, contenido);
    }
 
    private static void escribir(Path archivo, String contenido) {
        try {
            Files.writeString(archivo, contenido);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
 
    private void crearCopia(LocalDate dia) throws IOException {
        Files.createDirectories(carpeta);
        Files.writeString(carpeta.resolve(RespaldoAutomatico.nombreDe(dia)), "copia " + dia);
    }
 
    private List<String> nombresEnLaCarpeta() throws IOException {
        try (Stream<Path> archivos = Files.list(carpeta)) {
            return archivos.map(archivo -> archivo.getFileName().toString()).sorted().toList();
        }
    }
}