package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.DetallePartida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePartidaDAO extends JpaRepository<DetallePartida, Long> {}
