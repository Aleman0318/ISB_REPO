// src/main/java/com/sistemascontables/ISuiteBalance/Repositorios/EstadoFinancieroDAO.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.EstadoFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoFinancieroDAO extends JpaRepository<EstadoFinanciero, Long> {
    Optional<EstadoFinanciero> findFirstByTipoEstadoAndPeriodo(String tipoEstado, String periodo);
}
