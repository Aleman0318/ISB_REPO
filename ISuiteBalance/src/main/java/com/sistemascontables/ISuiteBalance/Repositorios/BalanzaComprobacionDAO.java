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

    /**
     * Balanza de comprobación agregada por cuenta.
     * Rango de movimientos: [desde .. hastaExcl)  ⇒ incluye TODO el día 'hasta' original.
     * Sin casteos a DATE para no romper índices si p.fecha es TIMESTAMP.
     */
    @Transactional(readOnly = true)
    @Query(value = """
        WITH mov AS (
            SELECT
                d.id_cuenta,
                COALESCE(SUM(COALESCE(d.montodebe,  0)), 0) AS debitos,
                COALESCE(SUM(COALESCE(d.montohaber, 0)), 0) AS creditos
            FROM public.tbl_detallepartida d
            JOIN public.tbl_partidas p
              ON p.id_partida = d.id_partida
            WHERE p.fecha >= :desde
              AND p.fecha <  :hastaExcl              -- ✅ día final excluyente (= hasta + 1)
            GROUP BY d.id_cuenta
        )
        SELECT
            c.id_cuenta                   AS idCuenta,
            c.codigo                      AS codigo,
            c.nombrecuenta                AS nombre,
            c.tipocuenta                  AS tipoCuenta,
            COALESCE(c.saldo_inicial, 0)  AS saldoInicial,
            COALESCE(mov.debitos, 0)      AS debitos,
            COALESCE(mov.creditos, 0)     AS creditos
        FROM public.tbl_cuentacontable c
        LEFT JOIN mov ON mov.id_cuenta = c.id_cuenta
        WHERE COALESCE(c.saldo_inicial,0) <> 0
           OR COALESCE(mov.debitos,0)      <> 0
           OR COALESCE(mov.creditos,0)     <> 0
        ORDER BY c.codigo ASC
        """, nativeQuery = true)
    List<Balanza> calcularBalanzaCompleta(
            @Param("desde") LocalDate desde,
            @Param("hastaExcl") LocalDate hastaExcl
    );
}
