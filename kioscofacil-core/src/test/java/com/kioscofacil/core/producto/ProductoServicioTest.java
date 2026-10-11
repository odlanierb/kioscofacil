/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author odlanierbermudez
 */
class ProductoServicioTest {

    private final ProductoServicio servicio
            = new ProductoServicio(new ProductoRepositorioEnMemoria());
    private final CodigoBarras codigo = new CodigoBarras("7790001000011");

    @Test
    void registraYLuegoEncuentraElProducto() {
        servicio.registrar(codigo, "Alfajor", Dinero.de("1200"));
        assertEquals("Alfajor", servicio.buscarPorCodigo(codigo).nombre());
    }

    @Test
    void noPermiteRegistrarDosVecesElMismoCodigo() {
        servicio.registrar(codigo, "Alfajor", Dinero.de("1200"));
        assertThrows(ReglaDeNegocioException.class,
                () -> servicio.registrar(codigo, "Otro", Dinero.de("900")));
    }

    @Test
    void buscarUnCodigoInexistenteFalla() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.buscarPorCodigo(codigo));
    }

    @Test
    void cambiaElPrecioDeUnProductoExistente() {
        servicio.registrar(codigo, "Alfajor", Dinero.de("1200"));
        servicio.cambiarPrecio(codigo, Dinero.de("1500"));
        assertEquals(Dinero.de("1500"), servicio.buscarPorCodigo(codigo).precio());
    }

    @Test
    void alRegistrarUnDuplicadoInformaCualProductoYaExistia() {
        servicio.registrar(codigo, "Alfajor", Dinero.de("1200"));
        ProductoDuplicadoException error = assertThrows(ProductoDuplicadoException.class,
                () -> servicio.registrar(codigo, "Otro", Dinero.de("900")));
        assertEquals("Alfajor", error.existente().nombre());
    }

    @Test
    void reemplazaUnProductoExistente() {
        servicio.registrar(codigo, "Alfajor", Dinero.de("1200"));
        servicio.reemplazar(codigo, "Alfajor triple", Dinero.de("1800"));
        assertEquals("Alfajor triple", servicio.buscarPorCodigo(codigo).nombre());
    }

    @Test
    void noPuedeReemplazarUnProductoInexistente() {
        assertThrows(ReglaDeNegocioException.class,
                () -> servicio.reemplazar(codigo, "Alfajor", Dinero.de("1200")));
    }
}
