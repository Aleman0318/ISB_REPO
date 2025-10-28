// src/main/java/com/sistemascontables/ISuiteBalance/Models/EstadoFinanciero.java
package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_estadofinanciero")
public class EstadoFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Long id;

    @Column(name = "tipo_estado", nullable = false, length = 50)
    private String tipoEstado; // p.ej. "BALANZA_COMPROBACION"

    @Column(name = "periodo", nullable = false, length = 80)
    private String periodo;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    // getters/setters
    public Long getId() { return id; }
    public String getTipoEstado() { return tipoEstado; }
    public void setTipoEstado(String tipoEstado) { this.tipoEstado = tipoEstado; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
}
