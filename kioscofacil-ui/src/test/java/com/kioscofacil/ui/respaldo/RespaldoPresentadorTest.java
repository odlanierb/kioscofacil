/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.respaldo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import com.kioscofacil.core.respaldo.Respaldador;
import com.kioscofacil.ui.compartido.Resultado;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class RespaldoPresentadorTest {
 
    private static final Path HABITUAL = Path.of("carpeta-habitual");
 
    private RespaldadorFalso respaldador;
    private RespaldoPresentador presentador;
 
    @BeforeEach
    void preparar() {
        respaldador = new RespaldadorFalso();
        presentador = new RespaldoPresentador(respaldador, HABITUAL);
    }
 
    @Test
    void conoceLaCarpetaHabitual() {
        assertEquals(HABITUAL, presentador.carpetaHabitual());
    }
 
    @Test
    void respaldarEnLaCarpetaHabitualUsaEsaCarpetaYDiceDondeQuedoElArchivo() {
        Resultado resultado = presentador.respaldarEnCarpetaHabitual();
 
        assertTrue(resultado.exito());
        assertEquals(HABITUAL, respaldador.carpetaRecibida);
        String archivo = HABITUAL.resolve("kiosco-prueba.db").toAbsolutePath().toString();
        assertTrue(resultado.mensaje().contains(archivo));
    }
 
    @Test
    void respaldarEnOtraCarpetaUsaLaCarpetaElegida() {
        Path pendrive = Path.of("pendrive");
 
        Resultado resultado = presentador.respaldarEn(pendrive);
 
        assertTrue(resultado.exito());
        assertEquals(pendrive, respaldador.carpetaRecibida);
    }
 
    @Test
    void siFallaDaUnErrorQueNombraLaCarpetaYSugiereQueHacer() {
        respaldador.falla = new UncheckedIOException("sin permiso", new java.io.IOException("x"));
        Path pendrive = Path.of("pendrive");
 
        Resultado resultado = presentador.respaldarEn(pendrive);
 
        assertFalse(resultado.exito());
        assertTrue(resultado.mensaje().contains(pendrive.toAbsolutePath().toString()));
        assertTrue(resultado.mensaje().contains("otra carpeta"));
    }
 
    @Test
    void despuesDeUnFalloSePuedeVolverAIntentar() {
        respaldador.falla = new IllegalStateException("disco lleno");
        assertFalse(presentador.respaldarEnCarpetaHabitual().exito());
 
        respaldador.falla = null;
 
        assertTrue(presentador.respaldarEnCarpetaHabitual().exito());
    }
 
    private static final class RespaldadorFalso implements Respaldador {
 
        private Path carpetaRecibida;
        private RuntimeException falla;
 
        @Override
        public Path respaldar(Path carpetaDestino) {
            if (falla != null) {
                throw falla;
            }
            carpetaRecibida = carpetaDestino;
            return carpetaDestino.resolve("kiosco-prueba.db");
        }
    }
}
 