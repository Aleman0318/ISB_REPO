// src/main/java/com/sistemascontables/ISuiteBalance/Repositorios/BalanceGeneralDAO.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.BalanceGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceGeneralDAO extends JpaRepository<BalanceGeneral, Long> {
    // de momento no necesitamos métodos extra
}
