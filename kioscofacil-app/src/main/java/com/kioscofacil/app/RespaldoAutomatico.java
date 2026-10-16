/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @author odlanierbermudez
 */
/**
 * Respaldo automático: deja una sola copia por día, con el nombre
 * {@code kiosco-auto-AAAA-MM-DD.db}, y borra las más viejas para no llenar el disco.
 * Solo toca los archivos con ese nombre: los respaldos manuales y cualquier otro archivo
 * de la carpeta quedan como están.
 */
final class RespaldoAutomatico {
 
    /** Cuántas copias automáticas se conservan (una por día, así que son 30 días). */
    static final int CANTIDAD_A_CONSERVAR = 30;
 
    private static final String PREFIJO = "kiosco-auto-";
    private static final String SUFIJO = ".db";
    private static final Pattern NOMBRE =
            Pattern.compile("kiosco-auto-(\\d{4}-\\d{2}-\\d{2})\\.db");
 
    private static final System.Logger LOG =
            System.getLogger(RespaldoAutomatico.class.getName());
 
    /** Quien sabe copiar la base de datos en el archivo indicado (que todavía no existe). */
    @FunctionalInterface
    interface Copiador {
 
        void copiarEn(Path archivo);
    }
 
    private record Copia(LocalDate dia, Path archivo) {
    }
 
    private final Path carpeta;
    private final Clock reloj;
    private final Copiador copiador;
    private final int cantidadAConservar;
 
    RespaldoAutomatico(Path carpeta, Clock reloj, Copiador copiador) {
        this(carpeta, reloj, copiador, CANTIDAD_A_CONSERVAR);
    }
 
    RespaldoAutomatico(Path carpeta, Clock reloj, Copiador copiador, int cantidadAConservar) {
        if (cantidadAConservar < 1) {
            throw new IllegalArgumentException("Hay que conservar al menos una copia");
        }
        this.carpeta = Objects.requireNonNull(carpeta);
        this.reloj = Objects.requireNonNull(reloj);
        this.copiador = Objects.requireNonNull(copiador);
        this.cantidadAConservar = cantidadAConservar;
    }
 
    static String nombreDe(LocalDate dia) {
        return PREFIJO + dia + SUFIJO;
    }
 
    /**
     * Deja la copia de hoy (reemplazando la de hoy si ya había una) y borra las
     * automáticas más viejas. Devuelve el archivo de hoy.
     * Si algo falla al copiar, la copia anterior de hoy se conserva intacta.
     */
    Path ejecutar() throws IOException {
        Files.createDirectories(carpeta);
        String nombre = nombreDe(LocalDate.now(reloj));
        Path definitivo = carpeta.resolve(nombre);
        Path temporal = carpeta.resolve(nombre + ".tmp");
 
        Files.deleteIfExists(temporal);
        try {
            copiador.copiarEn(temporal);
            Files.move(temporal, definitivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            borrarTemporal(temporal, e);
            throw e;
        }
        borrarLasMasViejas(definitivo);
        return definitivo;
    }
 
    private static void borrarTemporal(Path temporal, Exception causa) {
        try {
            Files.deleteIfExists(temporal);
        } catch (IOException e) {
            causa.addSuppressed(e);
        }
    }
 
    /** La copia de hoy nunca se borra. Si no se puede borrar una vieja, se sigue con las demás. */
    private void borrarLasMasViejas(Path aConservar) {
        List<Copia> copias;
        try {
            copias = copiasAutomaticas();
        } catch (IOException e) {
            LOG.log(System.Logger.Level.WARNING,
                    "No se pudieron revisar los respaldos automáticos en " + carpeta, e);
            return;
        }
        copias.sort(Comparator.comparing(Copia::dia).reversed());
        for (Copia copia : copias.subList(Math.min(cantidadAConservar, copias.size()), copias.size())) {
            if (copia.archivo().equals(aConservar)) {
                continue;
            }
            try {
                Files.deleteIfExists(copia.archivo());
            } catch (IOException e) {
                LOG.log(System.Logger.Level.WARNING,
                        "No se pudo borrar el respaldo viejo " + copia.archivo(), e);
            }
        }
    }
 
    private List<Copia> copiasAutomaticas() throws IOException {
        List<Copia> copias = new ArrayList<>();
        try (Stream<Path> archivos = Files.list(carpeta)) {
            for (Path archivo : archivos.toList()) {
                Matcher coincidencia = NOMBRE.matcher(archivo.getFileName().toString());
                if (coincidencia.matches() && Files.isRegularFile(archivo)) {
                    try {
                        copias.add(new Copia(LocalDate.parse(coincidencia.group(1)), archivo));
                    } catch (DateTimeParseException noEsUnaFecha) {
                        // Tiene forma de respaldo automático pero la fecha no existe: no es nuestro.
                    }
                }
            }
        }
        return copias;
    }
}
 