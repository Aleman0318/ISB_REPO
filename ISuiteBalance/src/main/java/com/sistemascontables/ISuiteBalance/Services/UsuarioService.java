package com.sistemascontables.ISuiteBalance.Services;

import org.springframework.transaction.annotation.Transactional;
import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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

    //Usamos optional para manejar el Exceptnullpointer o en resumen como puede existir como no
    public Optional<Usuario> finByCorreo(String correo){
        return usuarioDAO.findByCorreo(correo);
    }

    /** Verifica si el correo ya existe */
    public boolean verificarExistencia(String correo) {
        return usuarioDAO.findByCorreo(correo.toLowerCase()).isPresent();
    }

    //agregado por daigo
    // ⬇️ NUEVO: listar todos para la vista
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioDAO.findAll();
    }
    //

    /** Permite buscar por correo desde SecurityConfig */
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioDAO.findByCorreo(correo.toLowerCase());
    }
}
