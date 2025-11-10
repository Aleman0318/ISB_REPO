// models/FlujoEfectivo.java
package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_flujoefectivo")
public class FlujoEfectivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_flujo")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_estado")
    private EstadoFinanciero estado;

    @Column(name = "flujo_operativo")
    private BigDecimal flujoOperativo;

    @Column(name = "flujo_inversion")
    private BigDecimal flujoInversion;

    @Column(name = "flujo_financiamiento")
    private BigDecimal flujoFinanciamiento;

    // getters/setters
    public Long getId() { return id; }
    public EstadoFinanciero getEstado() { return estado; }
    public void setEstado(EstadoFinanciero e) { this.estado = e; }
    public BigDecimal getFlujoOperativo() { return flujoOperativo; }
    public void setFlujoOperativo(BigDecimal x) { this.flujoOperativo = x; }
    public BigDecimal getFlujoInversion() { return flujoInversion; }
    public void setFlujoInversion(BigDecimal x) { this.flujoInversion = x; }
    public BigDecimal getFlujoFinanciamiento() { return flujoFinanciamiento; }
    public void setFlujoFinanciamiento(BigDecimal x) { this.flujoFinanciamiento = x; }
}
