package com.sistemascontables.ISuiteBalance.Repositorios;

import com.sistemascontables.ISuiteBalance.Models.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaDAO extends JpaRepository<Partida, Long> {

}
