/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import com.kioscofacil.core.compartido.Dinero;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.producto.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author odlanierbermudez
 */
public class Venta {

    private final UUID id = UUID.randomUUID();
    private final List<LineaVenta> lineas = new ArrayList<>();
    private Cobro cobro;

    public void agregar(Producto producto, int cantidad) {
        if (estaCobrada()) {
            throw new ReglaDeNegocioException("La venta ya fue cobrada, no se puede modificar");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        for (int i = 0; i < lineas.size(); i++) {
            LineaVenta linea = lineas.get(i);
            if (linea.codigo().equals(producto.codigo())) {
                lineas.set(i, linea.conCantidad(linea.cantidad() + cantidad));
                return;
            }
        }
        lineas.add(LineaVenta.de(producto, cantidad));
    }

    public Cobro cobrar(MedioPago medio, Dinero recibido) {
        if (medio == null || recibido == null) {
            throw new IllegalArgumentException("Falta el medio de pago o el monto recibido");
        }
        if (estaCobrada()) {
            throw new ReglaDeNegocioException("La venta ya fue cobrada");
        }
        if (estaVacia()) {
            throw new ReglaDeNegocioException("No se puede cobrar una venta vacía");
        }
        Dinero total = total();
        if (recibido.esMenorQue(total)) {
            throw new ReglaDeNegocioException("El monto recibido no alcanza para cubrir el total");
        }
        Dinero vuelto = recibido.restar(total);
        if (medio != MedioPago.EFECTIVO && !vuelto.equals(Dinero.CERO)) {
            throw new ReglaDeNegocioException("Solo se da vuelto en efectivo");
        }
        cobro = new Cobro(medio, recibido, vuelto);
        return cobro;
    }

    public boolean estaCobrada() {
        return cobro != null;
    }

    public Optional<Cobro> cobro() {
        return Optional.ofNullable(cobro);
    }

    public List<LineaVenta> lineas() {
        return List.copyOf(lineas);
    }

    public boolean estaVacia() {
        return lineas.isEmpty();
    }

    public UUID id() {
        return id;
    }

    public Dinero total() {
        Dinero total = Dinero.CERO;
        for (LineaVenta linea : lineas) {
            total = total.sumar(linea.subtotal());
        }
        return total;
    }
}
