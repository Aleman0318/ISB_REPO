package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.DetallePartida;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetallePartidaDAO extends JpaRepository<DetallePartida, Integer> {

    List<DetallePartida> findByIdPartida(Integer idPartida);
    void deleteByIdPartida(Integer idPartida);

    // NUEVO: líneas con nombre de cuenta
    @Query(value = """
        SELECT 
          d.i_detalle   AS iDetalle,
          d.id_cuenta   AS idCuenta,
          c.nombrecuenta AS nombreCuenta,
          d.montodebe   AS montoDebe,
          d.montohaber  AS montoHaber
        FROM public.tbl_detallepartida d
        JOIN public.tbl_cuentacontable c ON c.id_cuenta = d.id_cuenta
        WHERE d.id_partida = :idPartida
        ORDER BY d.i_detalle
        """, nativeQuery = true)
    List<DetallePartidaView> listarLineasConNombre(@Param("idPartida") Integer idPartida);
}
