// models/CuentaContable.java
package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_cuentacontable")
public class CuentaContable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long idCuenta;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombrecuenta;

    @Column(nullable = false)
    private String tipocuenta; // ACTIVO, PASIVO, etc

    @Column(name = "saldo_inicial", nullable = false)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Column(name = "fecha_saldo_inicial")
    private LocalDate fechaSaldoInicial;

    // getters/setters
    public Long getIdCuenta() { return idCuenta; }
    public void setIdCuenta(Long idCuenta) { this.idCuenta = idCuenta; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombrecuenta() { return nombrecuenta; }
    public void setNombrecuenta(String nombrecuenta) { this.nombrecuenta = nombrecuenta; }
    public String getTipocuenta() { return tipocuenta; }
    public void setTipocuenta(String tipocuenta) { this.tipocuenta = tipocuenta; }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public LocalDate getFechaSaldoInicial() {
        return fechaSaldoInicial;
    }

    public void setFechaSaldoInicial(LocalDate fechaSaldoInicial) {
        this.fechaSaldoInicial = fechaSaldoInicial;
    }

    public CuentaContable() {
    }

    public CuentaContable(String codigo, String nombrecuenta, String tipocuenta) {
        this.codigo = codigo;
        this.nombrecuenta = nombrecuenta;
        this.tipocuenta = tipocuenta;
    }


}
