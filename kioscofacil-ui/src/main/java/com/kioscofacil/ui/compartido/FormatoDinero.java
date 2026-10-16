/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kioscofacil.ui.compartido;

import com.kioscofacil.core.compartido.Dinero;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 * @author odlanierbermudez
 */
/** Cómo se muestra y cómo se lee el dinero que escribe la persona. */
public final class FormatoDinero {
 
    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
 
    // Hasta 9 dígitos y, opcional, coma o punto con 1 o 2 decimales. "1.500" se rechaza
    // a propósito: no se puede saber si son mil quinientos o uno con cincuenta.
    private static final Pattern MONTO = Pattern.compile("\\d{1,9}([.,]\\d{1,2})?");
 
    private FormatoDinero() {
    }
 
    /** Dinero para mostrar, por ejemplo "$ 1.500,50". */
    public static String mostrar(Dinero dinero) {
        return String.format(ARGENTINA, "$ %,.2f", dinero.monto());
    }
 
    /**
     * Lee lo que escribió la persona: "1500", "1500,50", "1500.50" o "$ 800".
     * Si no es un monto válido lanza IllegalArgumentException con el mensaje indicado.
     */
    public static Dinero leer(String texto, String mensajeSiNoEsValido) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.startsWith("$")) {
            limpio = limpio.substring(1).trim();
        }
        if (!MONTO.matcher(limpio).matches()) {
            throw new IllegalArgumentException(mensajeSiNoEsValido);
        }
        return Dinero.de(limpio.replace(',', '.'));
    }
}