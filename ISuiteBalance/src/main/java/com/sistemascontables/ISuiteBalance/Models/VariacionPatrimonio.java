// models/VariacionPatrimonio.java
package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_variacionespatrimonio")
public class VariacionPatrimonio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variacion")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_estado")           // FK a tbl_estadofinanciero
    private EstadoFinanciero estado;

    @Column(name = "cambios_capital")
    private BigDecimal cambiosCapital;

    private BigDecimal dividendos;

    @Column(name = "utilidad_retenida")
    private BigDecimal utilidadRetenida;

    // getters / setters
    public Long getId() { return id; }
    public EstadoFinanciero getEstado() { return estado; }
    public void setEstado(EstadoFinanciero estado) { this.estado = estado; }
    public BigDecimal getCambiosCapital() { return cambiosCapital; }
    public void setCambiosCapital(BigDecimal cambiosCapital) { this.cambiosCapital = cambiosCapital; }
    public BigDecimal getDividendos() { return dividendos; }
    public void setDividendos(BigDecimal dividendos) { this.dividendos = dividendos; }
    public BigDecimal getUtilidadRetenida() { return utilidadRetenida; }
    public void setUtilidadRetenida(BigDecimal utilidadRetenida) { this.utilidadRetenida = utilidadRetenida; }
}
