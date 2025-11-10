package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReporteDAO extends JpaRepository<Reporte, Long> {

    Optional<Reporte> findByTipoReporteAndPeriodicidadAndPeriodoClave(
            String tipoReporte, String periodicidad, String periodoClave);

    List<Reporte> findByEstadoOrderByCreatedAtDesc(String estado);
}
