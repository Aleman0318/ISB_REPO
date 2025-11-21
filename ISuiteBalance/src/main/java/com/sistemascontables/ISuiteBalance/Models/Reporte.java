package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.sistemascontables.ISuiteBalance.Models.Usuario;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;


@Entity
@Table(name = "tbl_reporte",
        uniqueConstraints = @UniqueConstraint(
                name="uk_reporte_unico",
                columnNames = {"tipo_reporte","periodicidad","periodo_clave"}))
public class Reporte {

    @ManyToOne
    @JoinColumn(name = "id_usuario_creador")
    private Usuario usuarioCreador;

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte;

    @Column(name = "tipo_reporte", nullable = false, length = 30)
    private String tipoReporte; // BALANZA, MAYOR, DIARIO

    @Column(name = "periodicidad", nullable = false, length = 30)
    private String periodicidad; // MENSUAL/BIMESTRAL/TRIMESTRAL/ANUAL

    @Column(name = "periodo_clave", nullable = false, length = 20)
    private String periodoClave; // p.ej. 2025-11, 2025-B6, 2025-Q4, 2024

    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // PENDIENTE/APROBADO/RECHAZADO

    @Column(name = "comentario_revision")
    private String comentarioRevision;

    @Column(name = "total_debitos", precision = 18, scale = 2)
    private BigDecimal totalDebitos;

    @Column(name = "total_haber", precision = 18, scale = 2)
    private BigDecimal totalHaber;

    @Column(name = "saldo_final", precision = 18, scale = 2)
    private BigDecimal saldoFinal;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "archivo_url")
    private String archivoUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }

    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }

    public String getPeriodoClave() { return periodoClave; }
    public void setPeriodoClave(String periodoClave) { this.periodoClave = periodoClave; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getComentarioRevision() { return comentarioRevision; }
    public void setComentarioRevision(String comentarioRevision) { this.comentarioRevision = comentarioRevision; }

    public BigDecimal getTotalDebitos() { return totalDebitos; }
    public void setTotalDebitos(BigDecimal totalDebitos) { this.totalDebitos = totalDebitos; }

    public BigDecimal getTotalHaber() { return totalHaber; }
    public void setTotalHaber(BigDecimal totalHaber) { this.totalHaber = totalHaber; }

    public BigDecimal getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(BigDecimal saldoFinal) { this.saldoFinal = saldoFinal; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getArchivoUrl() { return archivoUrl; }
    public void setArchivoUrl(String archivoUrl) { this.archivoUrl = archivoUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
