package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;

public interface Balanza {
    Long getIdCuenta();
    String getCodigo();
    String getNombre();      // nombrecuenta
    BigDecimal getSaldoInicial();
    BigDecimal getDebitos();
    BigDecimal getCreditos();
}
