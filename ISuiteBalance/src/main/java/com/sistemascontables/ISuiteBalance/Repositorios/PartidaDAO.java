package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.Partida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PartidaDAO extends JpaRepository<Partida, Long> {

    @Query("""
    SELECT 
        p.id                   AS idPartida,
        p.fecha                AS fecha,
        p.concepto             AS concepto,
        u.nombre               AS nombreUsuario,
        COALESCE(SUM(d.montoDebe),  0) AS totalDebe,
        COALESCE(SUM(d.montoHaber), 0) AS totalHaber,
        df.nombreArchivo       AS docNombre
    FROM Partida p
    LEFT JOIN Usuario u        ON u.id_usuario = p.idUsuario
    LEFT JOIN DetallePartida d ON d.idPartida  = p.id
    LEFT JOIN DocumentoFuente df ON df.idPartida = p.id
    GROUP BY p.id, p.fecha, p.concepto, u.nombre, df.nombreArchivo
    ORDER BY p.fecha DESC, p.id DESC
    """)
    Page<PartidaResumen> listarResumen(Pageable pageable);

}
