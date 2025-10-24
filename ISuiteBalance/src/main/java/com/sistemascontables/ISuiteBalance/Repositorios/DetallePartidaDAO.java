package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.DetallePartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DetallePartidaDAO extends JpaRepository<DetallePartida, Long> {

    @Query("""
        SELECT
            d.id           AS idDetalle,
            d.idCuenta     AS idCuenta,
            c.nombrecuenta AS nombreCuenta,
            d.montoDebe    AS montoDebe,
            d.montoHaber   AS montoHaber
        FROM DetallePartida d
        JOIN CuentaContable c ON c.idCuenta = d.idCuenta
        WHERE d.idPartida = :idPartida
        ORDER BY d.id ASC
        """)
    List<DetallePartidaView> lineasConNombre(@Param("idPartida") Long idPartida);

    void deleteByIdPartida(Long idPartida);

    // >>> filas para Libro Diario (una por detalle), con filtro opcional por MES (rango)
    @Query("""
    SELECT
        d.id           AS idDetalle,
        d.idCuenta     AS idCuenta,
        p.id           AS idPartida,
        p.fecha        AS fecha,
        c.tipocuenta   AS tipocuenta,
        CONCAT(c.codigo, ' - ', c.nombrecuenta) AS cuenta,
        p.concepto     AS descripcion,
        d.montoDebe    AS debe,
        d.montoHaber   AS haber,
        df.id          AS docId,
        df.nombreArchivo AS docNombre
    FROM DetallePartida d
    JOIN Partida p            ON p.id = d.idPartida
    JOIN CuentaContable c     ON c.idCuenta = d.idCuenta
    LEFT JOIN DocumentoFuente df ON df.idPartida = p.id
    WHERE p.fecha BETWEEN COALESCE(:desde, p.fecha) AND COALESCE(:hasta, p.fecha)
    ORDER BY p.fecha ASC, p.id ASC, d.id ASC
""")
    List<LibroDiarioDAO> libroDiario(@Param("desde") LocalDate desde,
                                     @Param("hasta") LocalDate hasta
    );

    @Query("""
       SELECT
         p.fecha                    AS fecha,
         p.id                       AS idPartida,
         p.concepto                 AS descripcion,
         d.montoDebe                AS debe,
         d.montoHaber               AS haber,
         c.idCuenta                 AS idCuenta,
         c.codigo                   AS codigo,
         c.nombrecuenta             AS nombrecuenta,
         df.id                      AS docId,
         df.nombreArchivo           AS docNombre
       FROM DetallePartida d
       JOIN Partida p          ON p.id = d.idPartida
       JOIN CuentaContable c   ON c.idCuenta = d.idCuenta
       LEFT JOIN DocumentoFuente df ON df.idPartida = p.id
       WHERE (:idCuenta IS NULL OR d.idCuenta = :idCuenta)
         AND (COALESCE(:desde, p.fecha) <= p.fecha)
         AND (COALESCE(:hasta, p.fecha) >= p.fecha)
       ORDER BY c.codigo ASC, p.fecha ASC, p.id ASC, d.id ASC
    """)
    List<LibroMayorView> mayorMovimientos(
            @Param("idCuenta") Long idCuenta,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );
}
