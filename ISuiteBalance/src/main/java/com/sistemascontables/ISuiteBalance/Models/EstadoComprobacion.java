// src/main/java/com/sistemascontables/ISuiteBalance/Models/EstadoComprobacion.java
package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_estadocomprobacion")
public class EstadoComprobacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobacion")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoFinanciero estado; // FK a tbl_estadofinanciero

    @Column(name = "total_debe", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDebe = BigDecimal.ZERO;

    @Column(name = "total_haber", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalHaber = BigDecimal.ZERO;

    // getters/setters
    public Long getId() { return id; }
    public EstadoFinanciero getEstado() { return estado; }
    public void setEstado(EstadoFinanciero estado) { this.estado = estado; }
    public BigDecimal getTotalDebe() { return totalDebe; }
    public void setTotalDebe(BigDecimal totalDebe) { this.totalDebe = totalDebe; }
    public BigDecimal getTotalHaber() { return totalHaber; }
    public void setTotalHaber(BigDecimal totalHaber) { this.totalHaber = totalHaber; }
}
