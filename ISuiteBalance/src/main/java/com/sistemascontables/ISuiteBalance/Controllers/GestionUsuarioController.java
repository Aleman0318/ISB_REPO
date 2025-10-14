package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GestionUsuarioController {

    private final UsuarioService usuarioService;

    public GestionUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/crear-usuario")
    public String mostrarCrearUsuario() {
        return "CrearUsuario";
    }

    @PostMapping("/crear-usuario")
    public String crearUsuario(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam("contrasena") String passwordPlano,
            @RequestParam String rol,
            RedirectAttributes ra
    ) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo == null ? null : correo.trim().toLowerCase());
        u.setPasswordHash(passwordPlano); // <- texto plano aquí; el service la encripta
        u.setRol(rol);

        usuarioService.saveUsuario(u);

        ra.addFlashAttribute("ok", "Usuario creado correctamente.");
        return "redirect:/gestion-usuario";
    }
}
