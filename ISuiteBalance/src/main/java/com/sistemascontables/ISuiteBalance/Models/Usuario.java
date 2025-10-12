package com.sistemascontables.ISuiteBalance.Models;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tbl_usuarios")
public class Usuario implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;
   @Column(name = "nombre")
    private String nombre;
   @Column(name = "correo")
    private String correo;
   @Column(name = "password_hash")
    private String passwordHash;
   @Column(name = "rol")
    private String rol;

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String passwordHash, String rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo.toLowerCase();
    }

    public void setCorreo(String correo) {
        this.correo = correo.toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String password_hash) {
        this.passwordHash = password_hash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + rol); // Spring espera "ROLE_" prefix
    }

    @Override
    public String getUsername() {
        return correo; // Spring usa esto como "username"
    }

    @Override
    public String getPassword() {
        return passwordHash; // Spring usa esto como "password"
    }


    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
