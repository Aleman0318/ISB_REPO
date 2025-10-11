package com.sistemascontables.ISuiteBalance.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("nombreUsuario", "Ariana");
        model.addAttribute("tiempoActividad", "2h:43m:25s");
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
    public String registroLibroDiario() {
        return "RegistroLibroDiario";
    }

    @GetMapping("/subir-doc")
    public String subirDoc() {
        return "SubirDoc";
    }

    @GetMapping("/partida")
    public String registroPartida() {
        return "RegistroPartida";
    }
}
