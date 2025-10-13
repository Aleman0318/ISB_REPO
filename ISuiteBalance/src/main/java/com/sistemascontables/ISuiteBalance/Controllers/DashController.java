package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashController {

    //modificado por daigo
    private final UsuarioService usuarioService;

    // Inyección por constructor
    public DashController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    //

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails user, HttpSession session) {
        String nombre = "Invitado";
        if (user != null) {
            if (user instanceof Usuario u) {
                nombre = u.getNombre();
            } else {
                nombre = user.getUsername();
            }
        }

        Long loginTimeObj = (Long) session.getAttribute("loginTime");
        if (loginTimeObj == null) {
            // si no existe, inicialízalo aquí para evitar NPE
            loginTimeObj = System.currentTimeMillis();
            session.setAttribute("loginTime", loginTimeObj);
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
    public String bitacora() {
        return "Bitacora";
    }

    // ✅ GET: muestra el formulario con un objeto vacío para el binding
    @GetMapping("/crear-usuario")
    public String crearUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "CrearUsuario"; // templates/CrearUsuario.html
    }

    // ✅ POST: recibe el form, guarda y redirige con mensaje flash
    @PostMapping("/crear-usuario")
    public String crearUsuarioSubmit(
            @ModelAttribute("usuario") Usuario usuario,
            RedirectAttributes ra) {

        usuarioService.saveUsuario(usuario); // hashea internamente passwordHash
        ra.addFlashAttribute("msg", "Usuario creado correctamente");
        return "redirect:/gestion-usuario";
    }

    @GetMapping("/eliminar-usuario")
    public String eliminarUsuario() {
        return "EliminarUsuario";
    }

    @GetMapping("/generar-reporte")
    public String generarReporte() {
        return "GenerarReporte";
    }

    //  Ahora aquí cargamos los usuarios para la vista
    @GetMapping("/gestion-usuario")
    public String gestionUsuario(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "GestionUsuario"; // src/main/resources/templates/GestionUsuario.html
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
