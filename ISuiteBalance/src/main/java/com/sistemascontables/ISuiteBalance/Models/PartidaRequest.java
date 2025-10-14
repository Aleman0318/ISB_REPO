package com.sistemascontables.ISuiteBalance.Models;

import java.time.LocalDate;
import java.util.List;

public class PartidaRequest {
    private LocalDate fecha;
    private String concepto;
    private Long idUsuario; // id del usuario logueado
    private List<LineaDetalle> detalles; // lista de líneas

    // Getters y Setters
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public List<LineaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<LineaDetalle> detalles) { this.detalles = detalles; }
}
