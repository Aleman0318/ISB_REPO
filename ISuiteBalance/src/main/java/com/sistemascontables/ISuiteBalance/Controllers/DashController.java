package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Models.DocumentoReciente;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import com.sistemascontables.ISuiteBalance.Services.PartidaService;
import com.sistemascontables.ISuiteBalance.Services.ReporteService;
import com.sistemascontables.ISuiteBalance.Services.PartidaStatsService;
import com.sistemascontables.ISuiteBalance.Services.DocumentoRecienteService;
import com.sistemascontables.ISuiteBalance.Repositorios.AuditoriaDAO;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class DashController {

    // Servicios
    private final UsuarioService usuarioService;
    private final PartidaService partidaService;
    private final ReporteService reporteService;
    private final PartidaStatsService partidaStatsService;
    private final AuditoriaDAO auditoriaDAO;
    private final DocumentoRecienteService documentoRecienteService; // 👈 NUEVO

    // Inyección por constructor
    public DashController(UsuarioService usuarioService,
                          PartidaService partidaService,
                          ReporteService reporteService,
                          PartidaStatsService partidaStatsService,
                          AuditoriaDAO auditoriaDAO,
                          DocumentoRecienteService documentoRecienteService) { // 👈 NUEVO
        this.usuarioService = usuarioService;
        this.partidaService = partidaService;
        this.reporteService = reporteService;
        this.partidaStatsService = partidaStatsService;
        this.auditoriaDAO = auditoriaDAO;
        this.documentoRecienteService = documentoRecienteService; // 👈 NUEVO
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @AuthenticationPrincipal Object principal,
                            HttpSession session) {

        // ===== 1) Datos base tomados del principal =====
        String nombre = "Invitado";
        String correo = null;
        String rol    = null;
        String usernameOrCorreo = null; // normalmente el correo

        if (principal instanceof com.sistemascontables.ISuiteBalance.Models.Usuario u) {
            nombre = (u.getNombre() != null && !u.getNombre().isBlank())
                    ? u.getNombre()
                    : u.getCorreo();
            correo = u.getCorreo();
            rol    = u.getRol();
            usernameOrCorreo = u.getCorreo();
        } else if (principal instanceof UserDetails ud) {
            usernameOrCorreo = ud.getUsername();
            nombre = usernameOrCorreo;
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                Object p = auth.getPrincipal();
                if (p instanceof com.sistemascontables.ISuiteBalance.Models.Usuario u2) {
                    nombre = (u2.getNombre() != null && !u2.getNombre().isBlank())
                            ? u2.getNombre()
                            : u2.getCorreo();
                    correo = u2.getCorreo();
                    rol    = u2.getRol();
                    usernameOrCorreo = u2.getCorreo();
                } else if (p instanceof UserDetails ud2) {
                    usernameOrCorreo = ud2.getUsername();
                    nombre = usernameOrCorreo;
                }
            }
        }

        // ===== 2) Completar datos con la BD si hace falta =====
        if ((correo == null || rol == null)
                && usernameOrCorreo != null
                && !usernameOrCorreo.isBlank()) {
            Optional<Usuario> optU = usuarioService.findByCorreo(usernameOrCorreo);
            if (optU.isPresent()) {
                Usuario u = optU.get();

                if (u.getNombre() != null && !u.getNombre().isBlank()) {
                    nombre = u.getNombre();
                }
                correo = u.getCorreo();
                rol = u.getRol();
            }
        }

        // ===== 3) Tiempo de actividad (loginTime en sesión) =====
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

        // ===== 4) Contadores reales de reportes =====
        long pendientes  = reporteService.contarPendientes();
        long aprobados   = reporteService.contarAprobados();
        long rechazados  = reporteService.contarRechazados();

        long enRevision  = pendientes;                   // "en revisión" = pendientes
        long reportesProceso = pendientes + rechazados;  // en proceso

        // ===== 5) Datos para el gráfico de Partidas (últimos 5 meses) =====
        PartidaStatsService.PartidasChartData chartData =
                partidaStatsService.obtenerUltimos5Meses();
        model.addAttribute("partidasLabels", chartData.getLabels());
        model.addAttribute("partidasData",   chartData.getValores());

        // ===== 6) Contador real de Bitácora de Auditoría =====
        long bitacoraTotal = auditoriaDAO.count();
        model.addAttribute("bitacoraTotal", bitacoraTotal);

        // ===== 7) Documentos recientes desde carpeta docs =====
        List<DocumentoReciente> documentosRecientes =
                documentoRecienteService.listarRecientes(6);
        model.addAttribute("documentosRecientes", documentosRecientes);

        // ===== 8) Atributos para la vista =====
        model.addAttribute("nombreUsuario",  nombre);
        model.addAttribute("correoUsuario",  correo);
        model.addAttribute("rolUsuario",     rol);
        model.addAttribute("tiempoActividad", tiempoActividad);

        model.addAttribute("reportesProceso",   reportesProceso);
        model.addAttribute("reportesAprobados", aprobados);
        model.addAttribute("reportesRechazados", rechazados);
        model.addAttribute("reportesRevision",  enRevision);

        return "dashboard";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    // Listado de usuarios
    @GetMapping("/gestion-usuario")
    public String gestionUsuario(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "GestionUsuario";
    }

    @GetMapping("/eliminar-usuario")
    public String eliminarUsuario() { return "EliminarUsuario"; }

    @GetMapping("/generar-reporte")
    public String generarReporte() { return "GenerarReporte"; }

    @GetMapping("/modificar-usuario")
    public String modificarUsuario() { return "ModificarUsuario"; }

    @GetMapping("/perfil")
    public String perfil() { return "Perfil"; }

    @GetMapping("/about")
    public String about() { return "About"; }

    @GetMapping("/partida/{id}/ver")
    public String verDetallePartida(@PathVariable Integer id, Model model) {
        model.addAttribute("idPartida", id);
        model.addAttribute("lineas", partidaService.obtenerLineas(id));
        return "DetallePartida";
    }

}
