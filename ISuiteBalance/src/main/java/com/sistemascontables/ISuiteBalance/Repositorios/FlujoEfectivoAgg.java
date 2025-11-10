// repos/FlujoEfectivoAgg.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;

public interface FlujoEfectivoAgg {
    BigDecimal getDeltaActivosCorrientes();   // Δ AC (excluye Caja/Bancos)
    BigDecimal getDeltaPasivosCorrientes();   // Δ PC
    BigDecimal getDeltaActivosNoCorrientes(); // Δ ANC (104*,105*)
    BigDecimal getDividendos();               // Dividendos (egresos)
    BigDecimal getCambiosCapital();           // Aportes/Reducciones de capital (ingresos/egresos)
    BigDecimal getGastoDepreciacion();        // Gasto no monetario
}
