package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import com.sistemascontables.ISuiteBalance.Services.AuditoriaService;
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

    @Autowired
    private AuditoriaService auditoriaService;  // 👈 inyectamos el servicio de auditoría

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
                                   RedirectAttributes ra) {

        // Normalizamos correo para evitar duplicados con mayúsculas/minúsculas
        String correoNorm = correo == null ? "" : correo.trim().toLowerCase();
        String nombreNorm = nombre == null ? "" : nombre.trim();

        // 1) Correo duplicado
        if (usuarioService.verificarExistencia(correoNorm)) {
            ra.addAttribute("exists", "true");
            ra.addAttribute("msg", "El correo ingresado ya está en uso.");
            return "redirect:/register";
        }

        // 2) Política de contraseña
        // Al menos 8 caract., una mayúscula, una minúscula y un número:
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            ra.addAttribute("weak", "true");
            ra.addAttribute("msg", "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número.");
            return "redirect:/register";
        }

        // 3) Crear usuario con rol por defecto
        String rolPorDefecto = "Invitado";
        Usuario usuario = new Usuario(nombreNorm, correoNorm, password, rolPorDefecto);

        // Este método se encarga de encriptar la contraseña internamente
        usuarioService.saveUsuario(usuario);

        // ⚠️ Importante: después de guardar, el usuario ya tiene ID
        // 👉 Registramos la acción en la bitácora
        try {
            Long idActor = usuario.getId_usuario();  // el propio usuario que se acaba de registrar
            String accion = "REGISTRO_USUARIO";
            String entidad = "USUARIO";
            String descripcion = "El usuario se registró con el correo: " + correoNorm;

            auditoriaService.registrarAccion(idActor, accion, entidad, descripcion);
        } catch (Exception e) {
            // Si algo falla en la auditoría, NO rompemos el registro
            // podrías hacer log.error aquí si tienes logger
        }

        // 4) OK -> manda flag a login para SweetAlert
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }
}
