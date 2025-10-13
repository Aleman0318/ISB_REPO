package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // usa tu bean de Application

    /** Guarda usuario en BD con contraseña encriptada */
    public void saveUsuario(Usuario usuario) {
        usuario.setCorreo(usuario.getCorreo().toLowerCase());
        String hash = passwordEncoder.encode(usuario.getPasswordHash());
        usuario.setPasswordHash(hash);
        usuarioDAO.save(usuario);
    }

    /** Verifica si el correo ya existe */
    public boolean verificarExistencia(String correo) {
        return usuarioDAO.findByCorreo(correo.toLowerCase()).isPresent();
    }

    /** Permite buscar por correo desde SecurityConfig */
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioDAO.findByCorreo(correo.toLowerCase());
    }
}
