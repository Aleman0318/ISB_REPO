package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GestionUsuarioController {

    private final UsuarioService usuarioService;

    public GestionUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /* CREAR  */

    @GetMapping("/crear-usuario")
    public String mostrarCrearUsuario() {
        return "CrearUsuario";
    }

    @PostMapping("/crear-usuario")
    public String crearUsuario(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String correo,
            @RequestParam(name = "contrasena", required = false) String passwordPlano,
            @RequestParam(required = false) String rol,
            RedirectAttributes ra
    ) {
        String n = nombre == null ? "" : nombre.trim();
        String c = correo == null ? "" : correo.trim().toLowerCase();
        String p = passwordPlano == null ? "" : passwordPlano.trim();
        String r = rol == null ? "" : rol.trim().toUpperCase();

        if (n.isEmpty() || c.isEmpty() || p.isEmpty() || r.isEmpty()) {
            ra.addFlashAttribute("error", "Todos los campos son obligatorios.");
            ra.addFlashAttribute("formNombre", n);
            ra.addFlashAttribute("formCorreo", c);
            ra.addFlashAttribute("formRol", r);
            return "redirect:/crear-usuario";
        }
        if (!c.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            ra.addFlashAttribute("error", "Correo electrónico no válido.");
            ra.addFlashAttribute("formNombre", n);
            ra.addFlashAttribute("formCorreo", c);
            ra.addFlashAttribute("formRol", r);
            return "redirect:/crear-usuario";
        }
        if (p.length() < 6) {
            ra.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            ra.addFlashAttribute("formNombre", n);
            ra.addFlashAttribute("formCorreo", c);
            ra.addFlashAttribute("formRol", r);
            return "redirect:/crear-usuario";
        }
        if (!(r.equals("ADMIN") || r.equals("CONTADOR") || r.equals("AUDITOR"))) {
            ra.addFlashAttribute("error", "Rol inválido.");
            ra.addFlashAttribute("formNombre", n);
            ra.addFlashAttribute("formCorreo", c);
            ra.addFlashAttribute("formRol", r);
            return "redirect:/crear-usuario";
        }
        if (usuarioService.verificarExistencia(c)) {
            ra.addFlashAttribute("error", "Ya existe un usuario con ese correo.");
            ra.addFlashAttribute("formNombre", n);
            ra.addFlashAttribute("formCorreo", c);
            ra.addFlashAttribute("formRol", r);
            return "redirect:/crear-usuario";
        }

        Usuario u = new Usuario();
        u.setNombre(n);
        u.setCorreo(c);
        u.setPasswordHash(p); // el Service la encripta
        u.setRol(r);

        usuarioService.saveUsuario(u);
        ra.addFlashAttribute("ok", "Usuario creado correctamente.");
        return "redirect:/gestion-usuario";
    }

    /* ELIMINAR */

    @GetMapping("/usuario/eliminar/{id}")
    public String confirmarEliminacionUsuario(
            @PathVariable("id") Long id,
            org.springframework.ui.Model model,
            RedirectAttributes ra
    ) {
        return usuarioService.findById(id)
                .map(u -> { model.addAttribute("usuario", u); return "EliminarUsuario"; })
                .orElseGet(() -> { ra.addFlashAttribute("error", "Usuario no encontrado (ID " + id + ").");
                    return "redirect:/gestion-usuario"; });
    }

    @PostMapping("/usuario/eliminar")
    public String eliminarUsuarioConfirmado(
            @RequestParam("id") Long id,
            RedirectAttributes ra
    ) {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario actual) {
                if (actual.getId_usuario().equals(id)) {
                    ra.addFlashAttribute("error", "No puedes eliminar tu propio usuario.");
                    return "redirect:/gestion-usuario";
                }
            }
            usuarioService.eliminarPorId(id);
            ra.addFlashAttribute("ok", "Usuario eliminado correctamente (ID " + id + ").");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar el usuario (ID " + id + ").");
        }
        return "redirect:/gestion-usuario";
    }

    /* EDITAR */

    @GetMapping("/usuario/editar/{id}")
    public String editarUsuarioForm(
            @PathVariable("id") Long id,
            org.springframework.ui.Model model,
            RedirectAttributes ra
    ) {
        return usuarioService.findById(id)
                .map(u -> { model.addAttribute("usuario", u); return "ModificarUsuario"; })
                .orElseGet(() -> { ra.addFlashAttribute("error", "Usuario no encontrado (ID " + id + ").");
                    return "redirect:/gestion-usuario"; });
    }

    @PostMapping("/usuario/editar")
    public String editarUsuarioSubmit(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "correo", required = false) String correo,
            @RequestParam(name = "rol", required = false) String rol,
            @RequestParam(name = "action", required = false) String action,
            RedirectAttributes ra
    ) {
        // Si se pulsó "Cancelar", NO guardar
        if ("cancelar".equalsIgnoreCase(action)) {
            ra.addFlashAttribute("info", "Edición cancelada.");
            return "redirect:/gestion-usuario";
        }

        String n = nombre == null ? "" : nombre.trim();
        String c = correo == null ? "" : correo.trim().toLowerCase();
        String r = rol == null ? "" : rol.trim().toUpperCase();

        if (id == null || n.isEmpty() || c.isEmpty() || r.isEmpty()) {
            ra.addFlashAttribute("error", "Todos los campos son obligatorios.");
            return "redirect:/usuario/editar/" + (id == null ? "" : id);
        }
        if (!c.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            ra.addFlashAttribute("error", "Correo electrónico no válido.");
            return "redirect:/usuario/editar/" + id;
        }
        if (!(r.equals("ADMIN") || r.equals("CONTADOR") || r.equals("AUDITOR"))) {
            ra.addFlashAttribute("error", "Rol inválido.");
            return "redirect:/usuario/editar/" + id;
        }
        if (usuarioService.existeCorreoEnOtroUsuario(c, id)) {
            ra.addFlashAttribute("error", "Ya existe otro usuario con ese correo.");
            return "redirect:/usuario/editar/" + id;
        }

        return usuarioService.findById(id)
                .map(u -> {
                    u.setNombre(n);
                    u.setCorreo(c);
                    u.setRol(r);
                    usuarioService.saveSoloDatosBasicos(u); // no altera la contraseña
                    ra.addFlashAttribute("ok", "Usuario actualizado correctamente.");
                    return "redirect:/gestion-usuario";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Usuario no encontrado (ID " + id + ").");
                    return "redirect:/gestion-usuario";
                });
    }
}
