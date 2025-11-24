package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Mostrar formulario registro
    @GetMapping("/register")
    public String mostrarRegistro() {
        return "register"; // register.html
    }

    // Registrar usuario
    @PostMapping("/register")
    public String registrarUsuario(@RequestParam String nombre,
                                   @RequestParam String correo,
                                   @RequestParam String password,
                                   RedirectAttributes ra) { // 👈 agregado

        // Normalizamos correo para evitar duplicados con mayúsculas/minúsculas:
        String correoNorm = correo.trim().toLowerCase(); // opcional pero recomendado

        // 1) Correo duplicado
        if (usuarioService.verificarExistencia(correoNorm)) {
            ra.addAttribute("exists", "true");
            ra.addAttribute("msg", "El correo ingresado ya está en uso.");
            return "redirect:/register";
        }

        // 2) (Opcional) Política de contraseña
        // Al menos 8 caract., una mayúscula, una minúscula y un número:
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            ra.addAttribute("weak", "true");
            ra.addAttribute("msg", "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número.");
            return "redirect:/register";
        }

        // 3) Crear usuario
        String rolPorDefecto = "Invitado";
        Usuario usuario = new Usuario(nombre, correoNorm, password, rolPorDefecto);
        usuarioService.saveUsuario(usuario); // Aquí nos aseguramos que se encripte la contraseña

        // 4) OK -> manda flag a login para SweetAlert
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }
}