package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LibroMayorView {
    LocalDate getFecha();
    Long getIdPartida();
    String getDescripcion();
    BigDecimal getDebe();
    BigDecimal getHaber();

    // cuenta
    Long getIdCuenta();
    String getCodigo();
    String getNombrecuenta();

    // documento (opcional)
    Long getDocId();
    String getDocNombre();
}
