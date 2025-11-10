// repos/VariacionPatrimonioDAO.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.VariacionPatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface VariacionPatrimonioDAO extends JpaRepository<VariacionPatrimonio, Long> {

    @Query(value = """
        WITH mov AS (
          SELECT d.id_cuenta,
                 COALESCE(SUM(d.montodebe),  0) AS debitos,
                 COALESCE(SUM(d.montohaber), 0) AS creditos
          FROM public.tbl_detallepartida d
          JOIN public.tbl_partidas p
            ON p.id_partida = d.id_partida
          WHERE p.fecha BETWEEN :desde AND :hasta
          GROUP BY d.id_cuenta
        )
        SELECT
          /* Patrimonio (↑ en HABER). Excluye utilidades/dividendos explícitos por nombre */
          COALESCE(SUM(CASE
              WHEN UPPER(c.tipocuenta) = 'PATRIMONIO'
               AND c.nombrecuenta NOT ILIKE '%UTILIDAD%'
               AND c.nombrecuenta NOT ILIKE '%RESULTAD%'
               AND c.nombrecuenta NOT ILIKE '%DIVID%'
              THEN (COALESCE(m.creditos,0) - COALESCE(m.debitos,0))
              ELSE 0 END), 0) AS cambiosCapital,

          /* Dividendos registrados como cuentas de patrimonio con nombre tipo "Dividendos" */
          COALESCE(SUM(CASE
              WHEN UPPER(c.tipocuenta) = 'PATRIMONIO'
               AND c.nombrecuenta ILIKE '%DIVID%'
              THEN (COALESCE(m.debitos,0) - COALESCE(m.creditos,0))
              ELSE 0 END), 0) AS dividendos
        FROM public.tbl_cuentacontable c
        LEFT JOIN mov m ON m.id_cuenta = c.id_cuenta
        """, nativeQuery = true)
    VariacionPatrimonioAgg calcular(@Param("desde") LocalDate desde,
                                    @Param("hasta") LocalDate hasta);
}
