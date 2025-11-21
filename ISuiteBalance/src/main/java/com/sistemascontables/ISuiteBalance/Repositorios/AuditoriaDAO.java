package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaDAO extends JpaRepository<Auditoria, Long> {

    @Query("""
       SELECT a
       FROM Auditoria a
       WHERE (:idUsuario IS NULL OR a.usuario.id_usuario = :idUsuario)
         AND (:accion IS NULL OR a.accion = :accion)
         AND (:patronEntidad IS NULL OR a.entidadAfectada LIKE :patronEntidad)
         AND a.fecha >= COALESCE(:desde, a.fecha)
         AND a.fecha <= COALESCE(:hasta, a.fecha)
       ORDER BY a.fecha DESC
       """)
    List<Auditoria> buscarPorFiltros(
            @Param("idUsuario") Long idUsuario,
            @Param("accion") String accion,
            @Param("patronEntidad") String patronEntidad,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

}
