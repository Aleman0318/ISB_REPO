package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.DocumentoFuente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentoFuenteDAO extends JpaRepository<DocumentoFuente, Long> {
    Optional<DocumentoFuente> findByIdPartida(Long idPartida);
}
