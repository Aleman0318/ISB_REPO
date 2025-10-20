// repos/CuentaContableDAO.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuentaContableDAO extends JpaRepository<CuentaContable, Long> {
    List<CuentaContable> findAllByOrderByCodigoAsc();
}
