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

import java.util.Optional;

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

        // ===== 1) Datos base tomados del principal =====
        String nombre = "Invitado";
        String correo = null;
        String rol    = null;
        String usernameOrCorreo = null; // normalmente el correo

        if (principal instanceof com.sistemascontables.ISuiteBalance.Models.Usuario u) {
            nombre = (u.getNombre() != null && !u.getNombre().isBlank()) ? u.getNombre() : u.getCorreo();
            correo = u.getCorreo();
            rol    = u.getRol();                    // <-- String, sin .name()
            usernameOrCorreo = u.getCorreo();
        } else if (principal instanceof UserDetails ud) {
            usernameOrCorreo = ud.getUsername();    // suele ser el correo
            nombre = usernameOrCorreo;
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                Object p = auth.getPrincipal();
                if (p instanceof com.sistemascontables.ISuiteBalance.Models.Usuario u2) {
                    nombre = (u2.getNombre() != null && !u2.getNombre().isBlank()) ? u2.getNombre() : u2.getCorreo();
                    correo = u2.getCorreo();
                    rol    = u2.getRol();          // <-- String
                    usernameOrCorreo = u2.getCorreo();
                } else if (p instanceof UserDetails ud2) {
                    usernameOrCorreo = ud2.getUsername();
                    nombre = usernameOrCorreo;
                }
            }
        }

// ===== 2) Si faltan correo/rol, completar con la BD usando el correo único =====
        if ((correo == null || rol == null) && usernameOrCorreo != null && !usernameOrCorreo.isBlank()) {
            Optional<Usuario> optU = usuarioService.findByCorreo(usernameOrCorreo);
            if (optU.isPresent()) {
                Usuario u = optU.get();

                if (u.getNombre() != null && !u.getNombre().isBlank()) {
                    // “nombre bonito”: muestra nombre si existe; si no, deja el correo
                    nombre = u.getNombre();
                }
                correo = u.getCorreo();
                rol = u.getRol(); // ⚠️ Es String, así que sin .name()
            }
        }


        // ===== 3) Asegurar loginTime (lo que ya tenías) =====
        Long loginTimeObj = (Long) session.getAttribute("loginTime");
        if (loginTimeObj == null) {
            loginTimeObj = System.currentTimeMillis();
            session.setAttribute("loginTime", loginTimeObj);
        }
        long diff = System.currentTimeMillis() - loginTimeObj;
        long h = diff / (1000 * 60 * 60);
        long m = (diff / (1000 * 60)) % 60;
        long s = (diff / 1000) % 60;
        String tiempoActividad = String.format("%dh:%02dm:%02ds", h, m, s);

        // ===== 4) Atributos para la vista =====
        model.addAttribute("nombreUsuario",  nombre);  // visible en header
        model.addAttribute("correoUsuario",  correo);  // oculto para IndexedDB
        model.addAttribute("rolUsuario",     rol);     // oculto para IndexedDB

        model.addAttribute("tiempoActividad",    tiempoActividad);
        model.addAttribute("revisionesPendientes",150);
        model.addAttribute("reportesAprobados",  960);
        model.addAttribute("reportesRechazados", 220);
        model.addAttribute("reportesRevision",   150);
        model.addAttribute("bitacoraTotal",     "2.3k");

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

    @GetMapping("/perfil")
    public String perfil() { return "Perfil"; }

    @GetMapping("/about")
    public String about() { return "About"; }

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