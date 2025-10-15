package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.Partida;
import com.sistemascontables.ISuiteBalance.Repositorios.PartidaResumen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface PartidaDAO extends JpaRepository<Partida, Integer> {

    @Query(value = """
        SELECT 
          p.id_partida AS idPartida,
          p.fecha      AS fecha,
          p.concepto   AS concepto,
          u.nombre     AS nombreUsuario,
          COALESCE(SUM(d.montodebe), 0)  AS totalDebe,
          COALESCE(SUM(d.montohaber), 0) AS totalHaber
        FROM public.tbl_partidas p
        JOIN public.tbl_usuarios u ON u.id_usuario = p.id_usuario
        LEFT JOIN public.tbl_detallepartida d ON d.id_partida = p.id_partida
        GROUP BY p.id_partida, p.fecha, p.concepto, u.nombre
        ORDER BY p.fecha DESC, p.id_partida DESC
        """,
            countQuery = "SELECT COUNT(*) FROM public.tbl_partidas",
            nativeQuery = true)
    Page<PartidaResumen> listarResumen(Pageable pageable);
}
