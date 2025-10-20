package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import com.sistemascontables.ISuiteBalance.Services.PartidaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// [CAMBIO] imports para Advice/ModelAttribute (solo nombreUsuario global)
import org.springframework.web.bind.annotation.ControllerAdvice;   // [CAMBIO]
import org.springframework.web.bind.annotation.ModelAttribute;  // [CAMBIO]

@Controller
public class DashController {

    //modificado por daigo
    private final UsuarioService usuarioService;
    private final PartidaService partidaService;

    // Inyección por constructor
    public DashController(UsuarioService usuarioService, PartidaService partidaService) {
        this.usuarioService = usuarioService;
        this.partidaService = partidaService;
    }
    //

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal Object principal, HttpSession session) {

        // 1) Determinar nombre a mostrar
        String nombre = "Invitado";

        if (principal instanceof Usuario u) {
            // Si tu UserDetailsService retorna la entidad Usuario
            nombre = (u.getNombre() != null && !u.getNombre().isBlank()) ? u.getNombre() : u.getCorreo();
        } else if (principal instanceof UserDetails ud) {
            // Si retorna un UserDetails genérico, usamos el username (normalmente el correo)
            nombre = ud.getUsername();
        } else {
            // Último intento por si @AuthenticationPrincipal no inyectó
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                Object p = auth.getPrincipal();
                if (p instanceof Usuario u2) {
                    nombre = (u2.getNombre() != null && !u2.getNombre().isBlank()) ? u2.getNombre() : u2.getCorreo();
                } else if (p instanceof UserDetails ud2) {
                    nombre = ud2.getUsername();
                }
            }
        }

        // 2) Asegurar loginTime en sesión para evitar NPE  (NO TOCADO)
        Long loginTimeObj = (Long) session.getAttribute("loginTime");
        if (loginTimeObj == null) {
            loginTimeObj = System.currentTimeMillis();        // inicializa si no existe
            session.setAttribute("loginTime", loginTimeObj);  // guarda para futuras vistas
        }

        long diff = System.currentTimeMillis() - loginTimeObj;
        long h = diff / (1000 * 60 * 60);
        long m = (diff / (1000 * 60)) % 60;
        long s = (diff / 1000) % 60;
        String tiempoActividad = String.format("%dh:%02dm:%02ds", h, m, s);

        model.addAttribute("nombreUsuario", nombre);
        model.addAttribute("tiempoActividad", tiempoActividad);
        model.addAttribute("revisionesPendientes", 150);
        model.addAttribute("reportesAprobados", 960);
        model.addAttribute("reportesRechazados", 220);
        model.addAttribute("reportesRevision", 150);
        model.addAttribute("bitacoraTotal", "2.3k");

        return "dashboard";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/bitacora")
    public String bitacora() { return "Bitacora"; }

    // Listado de usuarios
    @GetMapping("/gestion-usuario")
    public String gestionUsuario(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "GestionUsuario"; // src/main/resources/templates/GestionUsuario.html
    }

    @GetMapping("/eliminar-usuario")
    public String eliminarUsuario() { return "EliminarUsuario"; }

    @GetMapping("/generar-reporte")
    public String generarReporte() { return "GenerarReporte"; }

    @GetMapping("/modificar-usuario")
    public String modificarUsuario() { return "ModificarUsuario"; }

    @GetMapping("/registro-libro-diario")
    public String registroLibroDiario() { return "RegistroLibroDiario"; }


    @GetMapping("/partida/{id}/ver")
    public String verDetallePartida(@PathVariable Integer id, Model model) {
        model.addAttribute("idPartida", id);
        model.addAttribute("lineas", partidaService.obtenerLineas(id)); // trae TODAS las líneas
        return "DetallePartida";
    }

    // 👉 Redirige desde /gestion-partida al listado real de partidas
    /*@GetMapping("/gestion-partida")
    public String redirigirGestionPartida() {
        return "redirect:/libro-diario";
    }*/


    // ❌ No definas /logout aquí: lo maneja Spring Security
    // @GetMapping("/logout")
    // public String logout() { return "redirect:/logout"; }
}

/* ===========================================================
   [CAMBIO] ADVICE GLOBAL (solo 'nombreUsuario', NO toca tiempo)
   Hace disponible ${nombreUsuario} en TODAS las vistas.
   =========================================================== */
@ControllerAdvice  // [CAMBIO]
class GlobalUserNameAdvice {  // [CAMBIO] clase no pública en el mismo archivo

    @ModelAttribute("nombreUsuario")  // [CAMBIO]
    public String nombreUsuario(@AuthenticationPrincipal Object principal) {
        String nombre = "Invitado";

        if (principal instanceof Usuario u) {
            return (u.getNombre() != null && !u.getNombre().isBlank()) ? u.getNombre() : u.getCorreo();
        } else if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                Object p = auth.getPrincipal();
                if (p instanceof Usuario u2) {
                    return (u2.getNombre() != null && !u2.getNombre().isBlank()) ? u2.getNombre() : u2.getCorreo();
                } else if (p instanceof UserDetails ud2) {
                    return ud2.getUsername();
                }
            }
        }
        return nombre;
    }
}
