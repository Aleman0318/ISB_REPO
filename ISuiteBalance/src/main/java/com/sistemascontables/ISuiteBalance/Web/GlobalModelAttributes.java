package com.sistemascontables.ISuiteBalance.Web;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Optional;

@ControllerAdvice
@Controller
public class GlobalModelAttributes {

    @Autowired
    private UsuarioService usuarioService;

    @org.springframework.web.bind.annotation.ModelAttribute
    public void addUserBasics(Model model, Authentication auth) {

        // valores por defecto (usuario no autenticado)
        String nombre = "Invitado";
        String correo = null;
        String rol    = "Invitado";

        if (auth != null && auth.isAuthenticated()) {
            // normalmente el username en Spring es el correo
            correo = auth.getName();

            Optional<Usuario> opt = usuarioService.findByCorreo(correo);
            if (opt.isPresent()) {
                Usuario u = opt.get();
                nombre = u.getNombre();   // campo directo en tu modelo
                rol    = u.getRol();      // "Administrador", "Contador", "Auditor" o "Invitado"
            } else {
                // si no se encontró, usar el correo como fallback
                nombre = (correo != null ? correo : "Invitado");
                rol    = "Invitado";
            }
        }

        // disponibles en TODAS las vistas Thymeleaf
        model.addAttribute("nombreUsuario", nombre);
        model.addAttribute("correoUsuario", correo);
        model.addAttribute("rolUsuario", rol);
    }
}
