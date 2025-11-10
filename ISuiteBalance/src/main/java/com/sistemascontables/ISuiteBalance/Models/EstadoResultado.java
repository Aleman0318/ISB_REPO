// models/EstadoResultado.java
package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_estadoresultado")
public class EstadoResultado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_estado")           // FK a tbl_estadofinanciero
    private EstadoFinanciero estado;

    private BigDecimal ingresos;
    private BigDecimal costos;
    private BigDecimal gastos;

    @Column(name = "utilidad_neta")
    private BigDecimal utilidadNeta;

    // getters/setters
    public Long getId() { return id; }
    public EstadoFinanciero getEstado() { return estado; }
    public void setEstado(EstadoFinanciero estado) { this.estado = estado; }
    public BigDecimal getIngresos() { return ingresos; }
    public void setIngresos(BigDecimal ingresos) { this.ingresos = ingresos; }
    public BigDecimal getCostos() { return costos; }
    public void setCostos(BigDecimal costos) { this.costos = costos; }
    public BigDecimal getGastos() { return gastos; }
    public void setGastos(BigDecimal gastos) { this.gastos = gastos; }
    public BigDecimal getUtilidadNeta() { return utilidadNeta; }
    public void setUtilidadNeta(BigDecimal utilidadNeta) { this.utilidadNeta = utilidadNeta; }
}
