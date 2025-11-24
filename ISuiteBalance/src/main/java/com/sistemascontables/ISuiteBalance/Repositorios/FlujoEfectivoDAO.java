// repos/FlujoEfectivoDAO.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface FlujoEfectivoDAO extends JpaRepository<com.sistemascontables.ISuiteBalance.Models.FlujoEfectivo, Long> {

    @Query(value = """
        /* Δ-saldos por grupo entre (hasta) y (desde-1) */
        WITH mov_ini AS (
          SELECT d.id_cuenta,
                 COALESCE(SUM(d.montodebe),0)  AS deb,
                 COALESCE(SUM(d.montohaber),0) AS hab
          FROM public.tbl_detallepartida d
          JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
          WHERE p.fecha < :desde
          GROUP BY d.id_cuenta
        ),
        mov_fin AS (
          SELECT d.id_cuenta,
                 COALESCE(SUM(d.montodebe),0)  AS deb,
                 COALESCE(SUM(d.montohaber),0) AS hab
          FROM public.tbl_detallepartida d
          JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
          WHERE p.fecha <= :hasta
          GROUP BY d.id_cuenta
        ),
        saldos AS (
          SELECT c.id_cuenta,
                 c.codigo,
                 UPPER(c.tipocuenta) AS tipocuenta,
                 c.nombrecuenta,
                 /* saldo = saldo_inicial + (deb - hab) */
                 (COALESCE(c.saldo_inicial,0) + COALESCE(fi.deb,0) - COALESCE(fi.hab,0)) AS saldo_fin,
                 (COALESCE(c.saldo_inicial,0) + COALESCE(ii.deb,0) - COALESCE(ii.hab,0)) AS saldo_ini
          FROM public.tbl_cuentacontable c
          LEFT JOIN mov_ini ii ON ii.id_cuenta = c.id_cuenta
          LEFT JOIN mov_fin fi ON fi.id_cuenta = c.id_cuenta
        )
        SELECT
          /* Activo Corriente ~ 101*,102*,103* (excluye caja/bancos 101-01 y 101-02) */
          COALESCE(SUM(CASE
            WHEN (s.codigo LIKE '101-%' OR s.codigo LIKE '102-%' OR s.codigo LIKE '103-%')
                 AND s.codigo NOT LIKE '101-01%' AND s.codigo NOT LIKE '101-02%'
            THEN (s.saldo_fin - s.saldo_ini) ELSE 0 END),0) AS deltaActivosCorrientes,

          /* Pasivo Corriente ~ 201*..205* */
          COALESCE(SUM(CASE
            WHEN (s.codigo LIKE '201-%' OR s.codigo LIKE '202-%' OR s.codigo LIKE '203-%'
               OR s.codigo LIKE '204-%' OR s.codigo LIKE '205-%')
            THEN (s.saldo_fin - s.saldo_ini) ELSE 0 END),0) AS deltaPasivosCorrientes,

          /* Activo No Corriente ~ 104*,105* */
          COALESCE(SUM(CASE
            WHEN (s.codigo LIKE '104-%' OR s.codigo LIKE '105-%')
            THEN (s.saldo_fin - s.saldo_ini) ELSE 0 END),0) AS deltaActivosNoCorrientes,

          /* Dividendos (patrimonio con nombre tipo "divid") */
          COALESCE(SUM(CASE
            WHEN s.tipocuenta = 'PATRIMONIO' AND s.nombrecuenta ILIKE '%divid%'
            THEN (s.saldo_fin - s.saldo_ini) ELSE 0 END),0) AS dividendos,

          /* Cambios de capital (PATRIMONIO excluyendo 'divid' y 'utilidad/resultado')
             Signo ajustado: aportes +, retiros - */
          COALESCE(SUM(CASE
            WHEN s.tipocuenta = 'PATRIMONIO'
             AND s.nombrecuenta NOT ILIKE '%divid%'
             AND s.nombrecuenta NOT ILIKE '%utilidad%'
             AND s.nombrecuenta NOT ILIKE '%resultado%'
            THEN (s.saldo_ini - s.saldo_fin) ELSE 0 END),0) AS cambiosCapital,

          /* Depreciación (no monetaria) en GASTO, para método indirecto */
          COALESCE((
            SELECT COALESCE(SUM(d.montodebe - d.montohaber),0)
            FROM public.tbl_detallepartida d
            JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
            JOIN public.tbl_cuentacontable c2 ON c2.id_cuenta = d.id_cuenta
            WHERE p.fecha BETWEEN :desde AND :hasta
              AND UPPER(c2.tipocuenta) = 'GASTO'
              AND c2.nombrecuenta ILIKE '%depreci%'
          ),0) AS gastoDepreciacion
        FROM saldos s
        """, nativeQuery = true)
    FlujoEfectivoAgg calcular(@Param("desde") LocalDate desde,
                              @Param("hasta") LocalDate hasta);
}
