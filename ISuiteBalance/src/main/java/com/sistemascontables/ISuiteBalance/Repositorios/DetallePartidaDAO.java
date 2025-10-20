package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.DetallePartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DetallePartidaDAO extends JpaRepository<DetallePartida, Long> {

    @Query("""
        SELECT
            d.id           AS idDetalle,
            d.idCuenta     AS idCuenta,
            c.nombrecuenta AS nombreCuenta,
            d.montoDebe    AS montoDebe,
            d.montoHaber   AS montoHaber
        FROM DetallePartida d
        JOIN CuentaContable c ON c.idCuenta = d.idCuenta
        WHERE d.idPartida = :idPartida
        ORDER BY d.id ASC
        """)
    List<DetallePartidaView> lineasConNombre(@Param("idPartida") Long idPartida);

    void deleteByIdPartida(Long idPartida);
}
