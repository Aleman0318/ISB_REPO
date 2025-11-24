package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;

public interface Balanza {
    Long getIdCuenta();
    String getCodigo();
    String getNombre();
    String getTipoCuenta();          // ACTIVO / PASIVO / ...
    BigDecimal getSaldoInicial();    // de c.saldo_inicial
    BigDecimal getDebitos();
    BigDecimal getCreditos();
}
