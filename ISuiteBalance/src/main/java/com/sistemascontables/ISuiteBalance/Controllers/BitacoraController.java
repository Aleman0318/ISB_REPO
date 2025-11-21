package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Auditoria;
import com.sistemascontables.ISuiteBalance.Models.Usuario;
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
import java.time.LocalTime;
import java.util.List;

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
            @RequestParam(value = "idUsuario", required = false) Long idUsuario,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "entidad", required = false) String entidad,
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model
    ) {

        // Lista de usuarios para el combo/filtro
        List<Usuario> usuarios = usuarioDAO.findAll();

        // Normalizar campos vacíos a null
        if (accion != null && accion.isBlank()) {
            accion = null;
        }
        if (entidad != null && entidad.isBlank()) {
            entidad = null;
        }

        // Construir patrón para LIKE (lo que usa el DAO)
        String patronEntidad = null;
        if (entidad != null) {
            patronEntidad = "%" + entidad.trim() + "%";
        }

        // Convertir LocalDate -> LocalDateTime (inicio y fin del día)
        LocalDateTime desdeDT = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime hastaDT = (hasta != null) ? hasta.atTime(LocalTime.MAX) : null;

        // Llamar al DAO con los filtros ya normalizados
        List<Auditoria> registros =
                auditoriaDAO.buscarPorFiltros(idUsuario, accion, patronEntidad, desdeDT, hastaDT);

        // Datos para la vista
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("registros", registros);

        // Para mantener los filtros seleccionados en el formulario (opcional pero útil)
        model.addAttribute("filtroIdUsuario", idUsuario);
        model.addAttribute("filtroAccion", accion);
        model.addAttribute("filtroEntidad", entidad);
        model.addAttribute("filtroDesde", desde);
        model.addAttribute("filtroHasta", hasta);

        // Nombre de la plantilla Thymeleaf
        return "Bitacora";   // o "bitacora" según se llame tu HTML
    }

}
