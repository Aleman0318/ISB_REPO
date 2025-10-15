package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;

import java.math.BigDecimal;

// DetallePartida.java
@Entity
@Table(name="tbl_detallepartida")
public class DetallePartida {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="i_detalle") private Long id;
    @Column(name="id_partida") private Long idPartida;
    @Column(name="id_cuenta") private Long idCuenta;
    @Column(name="montodebe") private BigDecimal montoDebe;
    @Column(name="montohaber") private BigDecimal montoHaber;

    public DetallePartida() {
    }

    public DetallePartida(Long idPartida, Long idCuenta, BigDecimal montoDebe, BigDecimal montoHaber) {
        this.idPartida = idPartida;
        this.idCuenta = idCuenta;
        this.montoDebe = montoDebe;
        this.montoHaber = montoHaber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Long idPartida) {
        this.idPartida = idPartida;
    }

    public Long getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Long idCuenta) {
        this.idCuenta = idCuenta;
    }

    public BigDecimal getMontoDebe() {
        return montoDebe;
    }

    public void setMontoDebe(BigDecimal montoDebe) {
        this.montoDebe = montoDebe;
    }

    public BigDecimal getMontoHaber() {
        return montoHaber;
    }

    public void setMontoHaber(BigDecimal montoHaber) {
        this.montoHaber = montoHaber;
    }
}
