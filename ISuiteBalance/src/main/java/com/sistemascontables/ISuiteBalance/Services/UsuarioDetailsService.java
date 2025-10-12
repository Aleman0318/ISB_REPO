package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        //  Convertir el correo ingresado a minúsculas antes de buscar en BD
        String correoNormalizado = (correo == null ? "" : correo).trim().toLowerCase(Locale.ROOT);

        return usuarioDAO.findByCorreo(correoNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
