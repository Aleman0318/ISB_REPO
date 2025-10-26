package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BalanzaComprobacionDAO extends JpaRepository<CuentaContable, Long> {

    @Transactional(readOnly = true)
    @Query(value = """
        SELECT
            c.id_cuenta    AS idCuenta,
            c.codigo       AS codigo,
            c.nombrecuenta AS nombre,

            COALESCE((
                SELECT SUM(d1.montodebe - d1.montohaber)
                FROM public.tbl_detallepartida d1
                JOIN public.tbl_partidas p1 ON p1.id_partida = d1.id_partida
                WHERE d1.id_cuenta = c.id_cuenta
                  AND p1.fecha < :desde
            ), 0) AS saldoInicial,

            COALESCE((
                SELECT SUM(d2.montodebe)
                FROM public.tbl_detallepartida d2
                JOIN public.tbl_partidas p2 ON p2.id_partida = d2.id_partida
                WHERE d2.id_cuenta = c.id_cuenta
                  AND p2.fecha >= :desde AND p2.fecha <= :hasta
            ), 0) AS debitos,

            COALESCE((
                SELECT SUM(d3.montohaber)
                FROM public.tbl_detallepartida d3
                JOIN public.tbl_partidas p3 ON p3.id_partida = d3.id_partida
                WHERE d3.id_cuenta = c.id_cuenta
                  AND p3.fecha >= :desde AND p3.fecha <= :hasta
            ), 0) AS creditos

        FROM public.tbl_cuentacontable c
        ORDER BY c.codigo ASC
    """, nativeQuery = true)
    List<Balanza> calcularBalanzaCompleta(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );
}
