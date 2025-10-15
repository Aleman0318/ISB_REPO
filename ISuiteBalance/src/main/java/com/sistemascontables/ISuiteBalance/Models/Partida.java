package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;

import java.time.LocalDate;

// Partida.java
@Entity
@Table(name="tbl_partidas")
public class Partida {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id_partida") private Long id;
    private LocalDate fecha;
    private String concepto;
    @Column(name="id_usuario") private Long idUsuario;
    // opcional: @OneToMany(mappedBy="partida") List<DetallePartida> detalles;


    public Partida() {
    }

    public Partida(LocalDate fecha, String concepto, Long idUsuario) {
        this.fecha = fecha;
        this.concepto = concepto;
        this.idUsuario = idUsuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}
