package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Repositorios.AuditoriaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/bitacora")
public class BitacoraController {

    private final AuditoriaDAO auditoriaDAO;
    private final UsuarioDAO usuarioDAO;

    public BitacoraController(AuditoriaDAO auditoriaDAO, UsuarioDAO usuarioDAO) {
        this.auditoriaDAO = auditoriaDAO;
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping
    public String verBitacora(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        // 🔥 NORMALIZAR ACCION ("", "TODAS", null → null real)
        if (accion != null) {
            accion = accion.trim();
            if (accion.isEmpty() ||
                    accion.equalsIgnoreCase("TODAS") ||
                    accion.equalsIgnoreCase("TODO") ||
                    accion.equalsIgnoreCase("ALL")) {
                accion = null;
            }
        }

        LocalDateTime desdeDT = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime hastaDT = (hasta != null) ? hasta.atTime(23,59,59) : null;

        String patronEntidad = "REPORTE"; // 👈 solo bitácora de reportes

        var registros = auditoriaDAO.buscarPorFiltros(
                idUsuario,
                accion,
                patronEntidad,
                desdeDT,
                hastaDT
        );

        model.addAttribute("usuarios", usuarioDAO.findAll());
        model.addAttribute("registros", registros);

        // filtros de vuelta a la vista
        model.addAttribute("filtroIdUsuario", idUsuario);
        model.addAttribute("filtroAccion", accion);
        model.addAttribute("filtroDesde", desde);
        model.addAttribute("filtroHasta", hasta);

        return "Bitacora";
    }



}
