package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;

public interface DetallePartidaView {
    Integer getIdDetalle();
    Integer getIdCuenta();
    String  getNombreCuenta();
    BigDecimal getMontoDebe();
    BigDecimal getMontoHaber();
}
