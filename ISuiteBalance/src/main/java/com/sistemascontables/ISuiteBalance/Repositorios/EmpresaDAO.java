package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaDAO extends JpaRepository<Empresa, Long> {
    // con esto basta por ahora (findAll, findById, save, etc.)
}
