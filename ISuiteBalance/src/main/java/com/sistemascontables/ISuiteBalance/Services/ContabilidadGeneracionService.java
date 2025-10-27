package com.sistemascontables.ISuiteBalance.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class ContabilidadGeneracionService {

    private static final Logger log = LoggerFactory.getLogger(ContabilidadGeneracionService.class);
    private final NamedParameterJdbcTemplate jdbc;

    public ContabilidadGeneracionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ==========================
    // LIBRO DIARIO (ya OK)
    // ==========================
    @Transactional
    public int generarLibroDiario(LocalDate desde, LocalDate hasta) {
        LocalDate d = (desde == null) ? LocalDate.of(1,1,1)     : desde;
        LocalDate h = (hasta == null) ? LocalDate.of(9999,12,31) : hasta;

        var p = new MapSqlParameterSource()
                .addValue("desde", d)
                .addValue("hasta", h);

        final String sql = """
            INSERT INTO public.tbl_librodiario (periodo, fecharegistro, id_partida)
            SELECT TO_CHAR(p.fecha,'YYYY-MM') AS periodo,
                   CURRENT_DATE              AS fecharegistro,
                   p.id_partida              AS id_partida
            FROM public.tbl_partidas p
            WHERE p.fecha BETWEEN :desde AND :hasta
              AND NOT EXISTS (
                    SELECT 1 FROM public.tbl_librodiario ld
                    WHERE ld.id_partida = p.id_partida
              )
            """;

        int n = jdbc.update(sql, p);
        log.info("[GEN-DIARIO] insertadas {} filas (rango {} .. {})", n, d, h);
        return n;
    }

    // ============ LIBRO MAYOR ============

    public record ResultadoMayor(String periodo, LocalDate rangoDesde, LocalDate rangoHasta,
                                 int borrados, int insertados) {}

    /**
     * Borra y vuelve a insertar el resumen del MAYOR para el periodo (mes) detectado.
     * Si no se pasa rango, usa el mes actual.
     */
    @Transactional
    public ResultadoMayor recalcularLibroMayorPeriodo(LocalDate desde, LocalDate hasta) {
        // 1) Determinar periodo (YYYY-MM)
        YearMonth ym = (desde != null) ? YearMonth.from(desde)
                : (hasta != null) ? YearMonth.from(hasta)
                : YearMonth.from(LocalDate.now());

        String periodo = String.format("%04d-%02d", ym.getYear(), ym.getMonthValue());
        LocalDate rangoDesde = ym.atDay(1);
        LocalDate rangoHasta = ym.atEndOfMonth();

        // 👉 Asegurar que haya filas en tbl_librodiario para ese mes
        generarLibroDiario(rangoDesde, rangoHasta);

        // 2) Borrar período previo
        int borrados = jdbc.update("""
        DELETE FROM public.tbl_libromayor
         WHERE periodo = :periodo
    """, new MapSqlParameterSource("periodo", periodo));

        // 3) Insertar resumen por cuenta
        var p = new MapSqlParameterSource()
                .addValue("periodo", periodo)
                .addValue("desde", rangoDesde)
                .addValue("hasta", rangoHasta);

        final String sqlInsert = """
        WITH movs AS (
            SELECT d.id_cuenta                    AS id_cuenta,
                   COALESCE(SUM(d.montodebe),0)  AS debitos,
                   COALESCE(SUM(d.montohaber),0) AS creditos
            FROM public.tbl_detallepartida d
            JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
            WHERE p.fecha BETWEEN :desde AND :hasta
            GROUP BY d.id_cuenta
        ),
        si AS (
            SELECT d.id_cuenta AS id_cuenta,
                   COALESCE(SUM(d.montodebe - d.montohaber),0) AS saldo_inicial
            FROM public.tbl_detallepartida d
            JOIN public.tbl_partidas p ON p.id_partida = d.id_partida
            WHERE p.fecha < :desde
            GROUP BY d.id_cuenta
        )
        INSERT INTO public.tbl_libromayor
            (periodo, fecha_registro, id_cuenta, saldo_inicial, saldo_final, id_librodiario)
        SELECT
            :periodo,
            CURRENT_DATE,
            m.id_cuenta,
            COALESCE(si.saldo_inicial,0) AS saldo_inicial,
            COALESCE(si.saldo_inicial,0) + COALESCE(m.debitos,0) - COALESCE(m.creditos,0) AS saldo_final,
            /* referenciar una fila real del diario del mismo periodo */
            COALESCE(
              (SELECT MIN(ld.id_librodiario)
                 FROM public.tbl_librodiario ld
                WHERE ld.periodo = :periodo),
              0  -- solo se usará si no hay FK; con la línea de generarLibroDiario, debería existir al menos una fila
            ) AS id_librodiario
        FROM movs m
        LEFT JOIN si ON si.id_cuenta = m.id_cuenta
    """;

        int insertados = jdbc.update(sqlInsert, p);
        log.info("[GEN-MAYOR] periodo={} rango={}..{} -> borrados={}, insertados={}",
                periodo, rangoDesde, rangoHasta, borrados, insertados);

        return new ResultadoMayor(periodo, rangoDesde, rangoHasta, borrados, insertados);
    }

}
