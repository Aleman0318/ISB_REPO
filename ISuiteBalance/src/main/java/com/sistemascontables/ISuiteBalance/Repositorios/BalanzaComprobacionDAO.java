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
        WITH si AS (
            SELECT d.id_cuenta,
                   SUM(d.montodebe - d.montohaber) AS saldo_inicial
            FROM public.tbl_detallepartida d
            JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
            WHERE p.fecha < :desde
            GROUP BY d.id_cuenta
        ),
        mov AS (
            SELECT d.id_cuenta,
                   SUM(d.montodebe)  AS debitos,
                   SUM(d.montohaber) AS creditos
            FROM public.tbl_detallepartida d
            JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
            WHERE p.fecha >= :desde AND p.fecha <= :hasta
            GROUP BY d.id_cuenta
        )
        SELECT
            c.id_cuenta      AS idCuenta,
            c.codigo         AS codigo,
            c.nombrecuenta   AS nombre,
            COALESCE(si.saldo_inicial, 0) AS saldoInicial,
            COALESCE(mov.debitos,      0) AS debitos,
            COALESCE(mov.creditos,     0) AS creditos
        FROM public.tbl_cuentacontable c
        LEFT JOIN si  ON si.id_cuenta  = c.id_cuenta
        LEFT JOIN mov ON mov.id_cuenta = c.id_cuenta
        -- 🔴 Solo cuentas afectadas (tienen saldo inicial distinto de 0 o movimientos en el rango)
        WHERE COALESCE(si.saldo_inicial,0) <> 0
           OR COALESCE(mov.debitos,0)      <> 0
           OR COALESCE(mov.creditos,0)     <> 0
        ORDER BY c.codigo ASC
    """, nativeQuery = true)
    List<Balanza> calcularBalanzaCompleta(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );
}
