package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;

import java.time.LocalDate;

// DocumentoFuente.java
@Entity
@Table(name="tbl_documentofuente") // OJO: usa el nombre exacto de tu tabla (parece 'tbl_documentofuente')
public class DocumentoFuente {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_documento") private Long id;
    @Column(name="nombrearchivo") private String nombreArchivo; // nombre físico guardado
    private LocalDate fecha;
    private String tipo; // "PDF"
    @Column(name="id_clasificacion") private Integer idClasificacion; // nullable
    @Column(name="id_partida") private Long idPartida; // se completa al guardar la partida

    public DocumentoFuente() {
    }

    public DocumentoFuente(String nombreArchivo, LocalDate fecha, String tipo, Integer idClasificacion, Long idPartida) {
        this.nombreArchivo = nombreArchivo;
        this.fecha = fecha;
        this.tipo = tipo;
        this.idClasificacion = idClasificacion;
        this.idPartida = idPartida;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getIdClasificacion() {
        return idClasificacion;
    }

    public void setIdClasificacion(Integer idClasificacion) {
        this.idClasificacion = idClasificacion;
    }

    public Long getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Long idPartida) {
        this.idPartida = idPartida;
    }
}
