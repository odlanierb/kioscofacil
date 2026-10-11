/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.core.venta;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author odlanierbermudez
 */
class VentaRepositorioEnMemoria implements VentaRepositorio {

    private final Map<UUID, VentaRealizada> ventas = new LinkedHashMap<>();

    @Override
    public void guardar(VentaRealizada venta) {
        ventas.put(venta.id(), venta);
    }

    @Override
    public Optional<VentaRealizada> buscarPorId(UUID id) {
        return Optional.ofNullable(ventas.get(id));
    }

    @Override
    public List<VentaRealizada> listarEntre(LocalDateTime desde, LocalDateTime hasta) {
        return ventas.values().stream()
                .filter(v -> !v.fecha().isBefore(desde) && v.fecha().isBefore(hasta))
                .toList();
    }
}
