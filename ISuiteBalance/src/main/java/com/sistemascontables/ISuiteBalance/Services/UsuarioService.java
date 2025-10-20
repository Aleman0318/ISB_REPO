package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Bean definido en tu Config

    /** CREA usuario: encripta contraseña y guarda. */
    @Transactional
    public void saveUsuario(Usuario usuario) {
        if (usuario.getCorreo() != null) {
            usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());
        }
        // passwordHash llega en texto plano desde el controller de "crear"
        String hash = passwordEncoder.encode(usuario.getPasswordHash());
        usuario.setPasswordHash(hash);
        usuarioDAO.saveAndFlush(usuario);
    }

    /** EDITA usuario: guarda nombre/correo/rol SIN tocar passwordHash. */
    @Transactional
    public Usuario saveSoloDatosBasicos(Usuario u) {
        if (u.getCorreo() != null) {
            u.setCorreo(u.getCorreo().trim().toLowerCase());
        }
        // No tocar u.getPasswordHash() aquí
        return usuarioDAO.saveAndFlush(u);
    }

    /** True si existe otro usuario con ese correo (ignora el idActual). */
    @Transactional(readOnly = true)
    public boolean existeCorreoEnOtroUsuario(String correo, Long idActual) {
        if (correo == null) return false;
        return usuarioDAO.findByCorreo(correo.trim().toLowerCase())
                .map(u -> !u.getId_usuario().equals(idActual))
                .orElse(false);
    }

    // Usamos Optional para búsquedas por correo
    public Optional<Usuario> finByCorreo(String correo) { // (conservar tu firma existente)
        return usuarioDAO.findByCorreo(correo);
    }

    /** Verifica existencia por correo (para alta). */
    @Transactional(readOnly = true)
    public boolean verificarExistencia(String correo) {
        return usuarioDAO.findByCorreo(correo == null ? null : correo.trim().toLowerCase()).isPresent();
    }

    /** Listado para la vista de gestión. */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioDAO.findAll();
    }

    /** Buscar por id. */
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioDAO.findById(id);
    }

    /** Eliminar por id. */
    @Transactional
    public void eliminarPorId(Long id) {
        usuarioDAO.deleteById(id);
    }

    /** Buscar por correo Security u otros usos. */
    @Transactional(readOnly = true)
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioDAO.findByCorreo(correo == null ? null : correo.trim().toLowerCase());
    }
}
