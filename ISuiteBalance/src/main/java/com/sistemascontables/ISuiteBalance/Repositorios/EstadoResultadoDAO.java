// repos/EstadoResultadoDAO.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.EstadoResultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface EstadoResultadoDAO extends JpaRepository<EstadoResultado, Long> {

    @Query(value = """
        WITH mov AS (
          SELECT d.id_cuenta,
                 COALESCE(SUM(d.montodebe),  0) AS debitos,
                 COALESCE(SUM(d.montohaber), 0) AS creditos
          FROM public.tbl_detallepartida d
          JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
          WHERE p.fecha BETWEEN :desde AND :hasta    -- fecha es DATE, BETWEEN es inclusivo
          GROUP BY d.id_cuenta
        )
        SELECT
          -- INGRESOS: creditos - debitos  (debe resta ingresos)
          COALESCE(SUM(CASE WHEN UPPER(c.tipocuenta) IN ('INGRESO','INGRESOS')
                            THEN mov.creditos - mov.debitos ELSE 0 END), 0) AS ingresos,
          -- COSTOS: debitos - creditos
          COALESCE(SUM(CASE WHEN UPPER(c.tipocuenta) IN ('COSTO','COSTOS')
                            THEN mov.debitos - mov.creditos ELSE 0 END), 0) AS costos,
          -- GASTOS: debitos - creditos
          COALESCE(SUM(CASE WHEN UPPER(c.tipocuenta) IN ('GASTO','GASTOS')
                            THEN mov.debitos - mov.creditos ELSE 0 END), 0) AS gastos
        FROM public.tbl_cuentacontable c
        LEFT JOIN mov ON mov.id_cuenta = c.id_cuenta
        """, nativeQuery = true)
    EstadoResultadoAgg calcular(@Param("desde") LocalDate desde,
                                @Param("hasta") LocalDate hasta);
}
