package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LibroDiarioDAO {
    Long getIdPartida();
    LocalDate getFecha();
    String getTipoCuenta();
    String getCuenta();        // código - nombre
    String getDescripcion();   // concepto de la partida
    BigDecimal getDebe();
    BigDecimal getHaber();
    Long getDocId();           // para enlazar /documento/{id}/ver
    String getDocNombre();     // nombre archivo
}
