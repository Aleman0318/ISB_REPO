package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import com.sistemascontables.ISuiteBalance.Services.AuditoriaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GestionUsuarioController {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    public GestionUsuarioController(UsuarioService usuarioService,
                                    AuditoriaService auditoriaService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    // ================== Helper: usuario actual ==================
    private Long obtenerIdUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario actual) {
            return actual.getId_usuario();
        }
        return null;
    }

    /* ================== CREAR ================== */

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
        String r = rol == null ? "" : rol.trim();

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
        if (!(r.equals("Administrador") || r.equals("Contador") || r.equals("Auditor") || r.equals("Invitado"))) {
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

        // ------ Auditoría: CREAR_USUARIO ------
        Long idActor = obtenerIdUsuarioActual();
        if (idActor != null) {
            auditoriaService.registrarAccion(
                    idActor,
                    "CREAR_USUARIO",
                    "USUARIO",
                    "Se creó el usuario: " + u.getCorreo()
            );
        }

        return "redirect:/gestion-usuario";
    }

    /* ================== ELIMINAR ================== */

    @GetMapping("/usuario/eliminar/{id}")
    public String confirmarEliminacionUsuario(
            @PathVariable("id") Long id,
            org.springframework.ui.Model model,
            RedirectAttributes ra
    ) {
        return usuarioService.findById(id)
                .map(u -> { model.addAttribute("usuario", u); return "EliminarUsuario"; })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Usuario no encontrado (ID " + id + ").");
                    return "redirect:/gestion-usuario";
                });
    }

    @PostMapping("/usuario/eliminar")
    public String eliminarUsuarioConfirmado(
            @RequestParam("id") Long id,
            RedirectAttributes ra
    ) {
        try {
            // Usuario que se va a eliminar (para el mensaje de auditoría)
            Usuario usuarioAEliminar = usuarioService.findById(id).orElse(null);

            var auth = SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario actual) {
                if (actual.getId_usuario().equals(id)) {
                    ra.addFlashAttribute("error", "No puedes eliminar tu propio usuario.");
                    return "redirect:/gestion-usuario";
                }
            }

            usuarioService.eliminarPorId(id);
            ra.addFlashAttribute("ok", "Usuario eliminado correctamente (ID " + id + ").");

            // ------ Auditoría: ELIMINAR_USUARIO ------
            Long idActor = obtenerIdUsuarioActual();
            if (idActor != null) {
                String correoEliminado = (usuarioAEliminar != null) ? usuarioAEliminar.getCorreo() : ("ID " + id);
                auditoriaService.registrarAccion(
                        idActor,
                        "ELIMINAR_USUARIO",
                        "USUARIO",
                        "Se eliminó el usuario: " + correoEliminado
                );
            }

        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar el usuario (ID " + id + ").");
        }
        return "redirect:/gestion-usuario";
    }

    /* ================== EDITAR ================== */

    @GetMapping("/usuario/editar/{id}")
    public String editarUsuarioForm(
            @PathVariable("id") Long id,
            org.springframework.ui.Model model,
            RedirectAttributes ra
    ) {
        return usuarioService.findById(id)
                .map(u -> { model.addAttribute("usuario", u); return "ModificarUsuario"; })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Usuario no encontrado (ID " + id + ").");
                    return "redirect:/gestion-usuario";
                });
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
        String r = rol == null ? "" : rol.trim();

        if (id == null || n.isEmpty() || c.isEmpty() || r.isEmpty()) {
            ra.addFlashAttribute("error", "Todos los campos son obligatorios.");
            return "redirect:/usuario/editar/" + (id == null ? "" : id);
        }
        if (!c.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            ra.addFlashAttribute("error", "Correo electrónico no válido.");
            return "redirect:/usuario/editar/" + id;
        }
        if (!(r.equals("Administrador") || r.equals("Contador") || r.equals("Auditor") || r.equals("Invitado"))) {
            ra.addFlashAttribute("error", "Rol inválido.");
            return "redirect:/usuario/editar/" + id;
        }
        if (usuarioService.existeCorreoEnOtroUsuario(c, id)) {
            ra.addFlashAttribute("error", "Ya existe otro usuario con ese correo.");
            return "redirect:/usuario/editar/" + id;
        }

        return usuarioService.findById(id)
                .map(u -> {
                    String correoAnterior = u.getCorreo();

                    u.setNombre(n);
                    u.setCorreo(c);
                    u.setRol(r);
                    usuarioService.saveSoloDatosBasicos(u); // no altera la contraseña
                    ra.addFlashAttribute("ok", "Usuario actualizado correctamente.");

                    // ------ Auditoría: EDITAR_USUARIO ------
                    Long idActor = obtenerIdUsuarioActual();
                    if (idActor != null) {
                        String desc = "Se editó el usuario. Antes: " + correoAnterior + " | Ahora: " + u.getCorreo();
                        auditoriaService.registrarAccion(
                                idActor,
                                "EDITAR_USUARIO",
                                "USUARIO",
                                desc
                        );
                    }

                    return "redirect:/gestion-usuario";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Usuario no encontrado (ID " + id + ").");
                    return "redirect:/gestion-usuario";
                });
    }
}
