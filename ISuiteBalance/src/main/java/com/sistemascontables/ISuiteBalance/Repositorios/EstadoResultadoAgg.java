// repos/EstadoResultadoAgg.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;

public interface EstadoResultadoAgg {
    BigDecimal getIngresos();
    BigDecimal getCostos();
    BigDecimal getGastos();
}
