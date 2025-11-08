package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaContableDAO extends JpaRepository<CuentaContable, Long> {

    // Listado ordenado por código (útil para catálogos / combos)
    List<CuentaContable> findAllByOrderByCodigoAsc();

    // Búsqueda por código contable para decidir si creamos o actualizamos
    Optional<CuentaContable> findByCodigo(String codigo);
}
