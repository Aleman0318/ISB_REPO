package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_partidas", schema = "public")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partida")
    private Integer idPartida;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "concepto", nullable = false, length = 500)
    private String concepto;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    public Integer getIdPartida() { return idPartida; }
    public void setIdPartida(Integer idPartida) { this.idPartida = idPartida; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
}
