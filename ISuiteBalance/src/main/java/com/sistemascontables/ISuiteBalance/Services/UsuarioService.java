package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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


    public boolean verificarExistencia(String correo) {
        return usuarioDAO.findByCorreo(correo.toLowerCase()).isPresent();
    }


}


