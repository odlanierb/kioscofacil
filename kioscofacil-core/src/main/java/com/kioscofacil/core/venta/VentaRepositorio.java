/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author odlanierbermudez
 */
public interface VentaRepositorio {

    void guardar(VentaRealizada venta);

    Optional<VentaRealizada> buscarPorId(UUID id);

    /**
     * Ventas con fecha desde (inclusive) hasta (exclusive).
     */
    List<VentaRealizada> listarEntre(LocalDateTime desde, LocalDateTime hasta);
}
