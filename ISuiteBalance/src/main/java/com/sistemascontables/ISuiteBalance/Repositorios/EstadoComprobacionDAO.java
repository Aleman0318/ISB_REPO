// src/main/java/com/sistemascontables/ISuiteBalance/Repositorios/EstadoComprobacionDAO.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.EstadoComprobacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoComprobacionDAO extends JpaRepository<EstadoComprobacion, Long> { }
