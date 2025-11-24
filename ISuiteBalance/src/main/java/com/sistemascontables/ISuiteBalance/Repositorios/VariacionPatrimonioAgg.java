// repos/VariacionPatrimonioAgg.java
package com.sistemascontables.ISuiteBalance.Repositorios;

import java.math.BigDecimal;

public interface VariacionPatrimonioAgg {
    BigDecimal getCambiosCapital();
    BigDecimal getDividendos();
}
