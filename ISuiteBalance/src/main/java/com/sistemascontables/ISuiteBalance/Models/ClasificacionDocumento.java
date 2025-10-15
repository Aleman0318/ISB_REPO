package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_clasificaciondocumento")
public class ClasificacionDocumento {

    @Id
    @Column(name = "id_clasificacion")
    private Integer idClasificacion;

    @Column(name = "nombreclasificacion", nullable = false, length = 100)
    private String nombreclasificacion;

    @Column(name = "descripcion")
    private String descripcion;

    // getters y setters
    public Integer getIdClasificacion() { return idClasificacion; }
    public void setIdClasificacion(Integer idClasificacion) { this.idClasificacion = idClasificacion; }

    public String getNombreclasificacion() { return nombreclasificacion; }
    public void setNombreclasificacion(String nombreclasificacion) { this.nombreclasificacion = nombreclasificacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
