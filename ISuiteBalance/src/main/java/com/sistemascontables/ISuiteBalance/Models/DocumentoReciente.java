package com.sistemascontables.ISuiteBalance.Models;

import java.time.LocalDateTime;

public class DocumentoReciente {

    private String nombre;
    private LocalDateTime fechaModificacion;

    public DocumentoReciente(String nombre, LocalDateTime fechaModificacion) {
        this.nombre = nombre;
        this.fechaModificacion = fechaModificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }
}
