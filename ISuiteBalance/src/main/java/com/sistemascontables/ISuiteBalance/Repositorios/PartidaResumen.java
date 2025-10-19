package com.sistemascontables.ISuiteBalance.Repositorios;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PartidaResumen {
    Integer getIdPartida();
    LocalDate getFecha();
    String getConcepto();
    String getNombreUsuario();
    BigDecimal getTotalDebe();
    BigDecimal getTotalHaber();
    String getDocNombre();
}
