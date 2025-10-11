package com.sistemascontables.ISuiteBalance.Controllers;
import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
                                   @RequestParam String password) {
        if (usuarioService.verificarExistencia(correo)) {
            // Redirige al formulario con un mensaje
            return "redirect:/register?error=correo";
        }

        // Asignar un rol por defecto (temporal)
        String rolPorDefecto = "CONTADOR";

        Usuario usuario = new Usuario(nombre, correo, password, rolPorDefecto);
        usuarioService.saveUsuario(usuario);

        return "redirect:/login";
    }


    @GetMapping("/login")
    public String mostrarLogin() {
        return "login"; // sin extensión .html
    }



    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario == null) {
            return "redirect:/login"; // fallback si no está autenticado
        }

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("rol", usuario.getRol());
        return "dashboard";
    }


}
