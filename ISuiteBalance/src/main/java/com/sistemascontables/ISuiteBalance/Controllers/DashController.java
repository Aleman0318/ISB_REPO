package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails user, HttpSession session) {

        // Nombre por defecto si no hay sesion
        String nombre = "Invitado";

        if (user != null) {
            //tomamos el "nombre" (no el correo)
            if (user instanceof Usuario) {
                nombre = ((Usuario) user).getNombre();
            } else {
                // Si no, usamos el username (el correo)
                nombre = user.getUsername();
            }
        }

        // Calcular tiempo de actividad
        long loginTime = (Long) session.getAttribute("loginTime");
        long diff = System.currentTimeMillis() - loginTime;
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
    public String bitacora() {
        return "Bitacora";
    }

    @GetMapping("/crear-usuario")
    public String crearUsuario() {
        return "CrearUsuario";
    }

    @GetMapping("/eliminar-usuario")
    public String eliminarUsuario() {
        return "EliminarUsuario";
    }

    @GetMapping("/generar-reporte")
    public String generarReporte() {
        return "GenerarReporte";
    }

    @GetMapping("/gestion-usuario")
    public String gestionUsuario() {
        return "GestionUsuario";
    }

    @GetMapping("/modificar-usuario")
    public String modificarUsuario() {
        return "ModificarUsuario";
    }

    @GetMapping("/registro-libro-diario")
    public String registroLibroDiario() {return "RegistroLibroDiario";}

    @GetMapping("/subir-doc")
    public String subirDoc() {
        return "SubirDoc";
    }

    @GetMapping("/partida")
    public String registroPartida() {
        return "RegistroPartida";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/logout";   // Spring Security hace el logout y redirige
    }
}
