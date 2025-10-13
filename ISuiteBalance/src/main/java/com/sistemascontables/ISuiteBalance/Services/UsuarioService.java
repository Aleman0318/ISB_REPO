package com.sistemascontables.ISuiteBalance.Services;

import org.springframework.transaction.annotation.Transactional;
import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    public void saveUsuario(Usuario usuario) {
        String hash = passwordEncoder.encode(usuario.getPasswordHash()); //
        usuario.setPasswordHash(hash);
        usuario.setCorreo(usuario.getCorreo().toLowerCase());
        usuarioDAO.save(usuario);
    }

    //Usamos optional para manejar el Exceptnullpointer o en resumen como puede existir como no
    public Optional<Usuario> finByCorreo(String correo){
        return usuarioDAO.findByCorreo(correo);
    }

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

}


