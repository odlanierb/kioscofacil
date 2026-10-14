/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  odlanierbermudez
 * Created: 13 oct 2026
 */
CREATE TABLE IF NOT EXISTS producto (
    codigo           TEXT    PRIMARY KEY,
    nombre           TEXT    NOT NULL,
    precio_centavos  INTEGER NOT NULL CHECK (precio_centavos >= 0)
);

CREATE TABLE IF NOT EXISTS stock (
    codigo    TEXT    PRIMARY KEY REFERENCES producto (codigo),
    cantidad  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS venta (
    id                 TEXT    PRIMARY KEY,
    fecha              TEXT    NOT NULL,
    medio_pago         TEXT    NOT NULL,
    recibido_centavos  INTEGER NOT NULL CHECK (recibido_centavos >= 0),
    vuelto_centavos    INTEGER NOT NULL CHECK (vuelto_centavos >= 0)
);

CREATE TABLE IF NOT EXISTS linea_venta (
    venta_id                  TEXT    NOT NULL REFERENCES venta (id),
    codigo                    TEXT    NOT NULL,
    nombre                    TEXT    NOT NULL,
    precio_unitario_centavos  INTEGER NOT NULL CHECK (precio_unitario_centavos >= 0),
    cantidad                  INTEGER NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (venta_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_venta_fecha ON venta (fecha);

