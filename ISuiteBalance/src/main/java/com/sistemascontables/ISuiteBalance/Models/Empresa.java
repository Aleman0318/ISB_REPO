package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "giro", length = 150)
    private String giro;

    @Column(name = "nit", length = 20)
    private String nit;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    // ===== Getters y Setters =====
    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGiro() {
        return giro;
    }

    public void setGiro(String giro) {
        this.giro = giro;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
