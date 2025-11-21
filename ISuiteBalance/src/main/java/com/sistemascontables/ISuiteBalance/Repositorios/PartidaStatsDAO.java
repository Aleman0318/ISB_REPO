package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio ligero SOLO para estadísticas de partidas (dashboard).
 * Usa una consulta nativa a la tabla tbl_partidas.
 */
public interface PartidaStatsDAO extends JpaRepository<Partida, Integer> {

    @Query(value = """
            SELECT to_char(fecha, 'YYYY-MM') AS periodo,
                   COUNT(*)                  AS total
            FROM tbl_partidas
            WHERE fecha >= :desde
            GROUP BY periodo
            ORDER BY periodo
            """, nativeQuery = true)
    List<Object[]> contarPartidasPorMesDesde(@Param("desde") LocalDate desde);
}
