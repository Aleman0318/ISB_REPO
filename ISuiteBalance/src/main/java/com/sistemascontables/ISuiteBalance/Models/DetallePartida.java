package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_detallepartida", schema = "public")
public class DetallePartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_detalle")   // OJO: i_detalle en tu BD
    private Integer iDetalle;

    @Column(name = "id_partida", nullable = false)
    private Integer idPartida;

    @Column(name = "id_cuenta", nullable = false)
    private Integer idCuenta;

    @Column(name = "montodebe", nullable = false)
    private BigDecimal montoDebe = BigDecimal.ZERO;

    @Column(name = "montohaber", nullable = false)
    private BigDecimal montoHaber = BigDecimal.ZERO;

    public Integer getiDetalle() { return iDetalle; }
    public void setiDetalle(Integer iDetalle) { this.iDetalle = iDetalle; }
    public Integer getIdPartida() { return idPartida; }
    public void setIdPartida(Integer idPartida) { this.idPartida = idPartida; }
    public Integer getIdCuenta() { return idCuenta; }
    public void setIdCuenta(Integer idCuenta) { this.idCuenta = idCuenta; }
    public BigDecimal getMontoDebe() { return montoDebe; }
    public void setMontoDebe(BigDecimal montoDebe) { this.montoDebe = montoDebe; }
    public BigDecimal getMontoHaber() { return montoHaber; }
    public void setMontoHaber(BigDecimal montoHaber) { this.montoHaber = montoHaber; }
}
