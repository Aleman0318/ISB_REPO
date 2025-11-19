// src/main/java/com/sistemascontables/ISuiteBalance/Models/BalanceGeneral.java
package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_balance")
public class BalanceGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_balance")
    private Long idBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoFinanciero estado;

    @Column(name = "periodo", length = 50)
    private String periodo;

    @Column(name = "total_activos")
    private BigDecimal totalActivos;

    @Column(name = "total_pasivos")
    private BigDecimal totalPasivos;

    @Column(name = "utilidad")
    private BigDecimal utilidad;

    // ===== Getters y Setters =====
    public Long getIdBalance() { return idBalance; }
    public void setIdBalance(Long idBalance) { this.idBalance = idBalance; }

    public EstadoFinanciero getEstado() { return estado; }
    public void setEstado(EstadoFinanciero estado) { this.estado = estado; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public BigDecimal getTotalActivos() { return totalActivos; }
    public void setTotalActivos(BigDecimal totalActivos) { this.totalActivos = totalActivos; }

    public BigDecimal getTotalPasivos() { return totalPasivos; }
    public void setTotalPasivos(BigDecimal totalPasivos) { this.totalPasivos = totalPasivos; }

    public BigDecimal getUtilidad() { return utilidad; }
    public void setUtilidad(BigDecimal utilidad) { this.utilidad = utilidad; }
}
