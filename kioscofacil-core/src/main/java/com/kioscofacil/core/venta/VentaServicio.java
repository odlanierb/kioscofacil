/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import com.kioscofacil.core.compartido.EjecutorTransaccional;
import com.kioscofacil.core.compartido.ReglaDeNegocioException;
import com.kioscofacil.core.stock.StockServicio;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
/**
 *
 * @author odlanierbermudez
 */
public class VentaServicio {

    private final VentaRepositorio repositorio;
    private final Clock reloj;
    private final StockServicio stock;
    private final EjecutorTransaccional transaccion;

    public VentaServicio(VentaRepositorio repositorio, Clock reloj, StockServicio stock,
            EjecutorTransaccional transaccion) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.reloj = Objects.requireNonNull(reloj);
        this.stock = Objects.requireNonNull(stock);
        this.transaccion = Objects.requireNonNull(transaccion);
    }

    public VentaRealizada registrar(Venta venta) {
        Cobro cobro = venta.cobro().orElseThrow(
                () -> new ReglaDeNegocioException("La venta todavía no fue cobrada"));
        return transaccion.ejecutar(() -> {
            if (repositorio.buscarPorId(venta.id()).isPresent()) {
                throw new ReglaDeNegocioException("Esta venta ya fue registrada");
            }
            VentaRealizada realizada = new VentaRealizada(
                    venta.id(), LocalDateTime.now(reloj), venta.lineas(), cobro);
            repositorio.guardar(realizada);
            for (LineaVenta linea : realizada.lineas()) {
                stock.descontar(linea.codigo(), linea.cantidad());
            }
            return realizada;
        });
    }

    public List<VentaRealizada> ventasDelDia(LocalDate dia) {
        return repositorio.listarEntre(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }
}
