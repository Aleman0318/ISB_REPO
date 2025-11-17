package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.ReporteService;
import com.sistemascontables.ISuiteBalance.Services.BalanzaComprobacionService;
import com.sistemascontables.ISuiteBalance.Services.LibroDiarioService;
import com.sistemascontables.ISuiteBalance.Services.LibroMayorService;
import com.sistemascontables.ISuiteBalance.Models.Periodo;
import com.sistemascontables.ISuiteBalance.Models.Reporte;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService service;
    private final BalanzaComprobacionService balanzaService;
    private final LibroDiarioService libroDiarioService;
    private final LibroMayorService libroMayorService;

    public ReporteController(ReporteService service,
                             BalanzaComprobacionService balanzaService,
                             LibroDiarioService libroDiarioService,
                             LibroMayorService libroMayorService) {
        this.service = service;
        this.balanzaService = balanzaService;
        this.libroDiarioService = libroDiarioService;
        this.libroMayorService = libroMayorService;
    }

    // Lista general (el Auditor la usa)
    @PreAuthorize("hasAnyRole('Administrador','Contador','Auditor')")
    @GetMapping
    public String listar(Model model) {
        var pendientes = service.listarPendientes();
        var aprobados = service.listarAprobados();
        var rechazados = service.listarRechazados();

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("aprobados", aprobados);
        model.addAttribute("rechazados", rechazados);
        return "ReportesLista";
    }

    // Mis reportes (Admin + Contador)
    @GetMapping("/mis")
    @PreAuthorize("hasAnyRole('Administrador','Contador')")
    public String misReportes(Model model) {
        model.addAttribute("pendientes", service.listarPendientes());
        model.addAttribute("rechazados", service.listarRechazados());
        model.addAttribute("aprobados",  service.listarAprobados());
        return "MisReportes";
    }

    // Form nuevo / editar
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('Administrador','Contador')")
    public String nuevoForm(@RequestParam(value = "from", required = false) Long from,
                            Model model,
                            RedirectAttributes ra) {

        if (from != null) {
            // Venimos desde "Editar" de un reporte RECHAZADO
            try {
                Reporte r = service.buscarPorId(from);
                if (!"RECHAZADO".equals(r.getEstado())) {
                    ra.addFlashAttribute("err", "Solo los reportes RECHAZADOS pueden editarse.");
                    return "redirect:/reportes/mis";
                }
                model.addAttribute("reporteEdit", r);
            } catch (IllegalArgumentException ex) {
                ra.addFlashAttribute("err", ex.getMessage());
                return "redirect:/reportes/mis";
            }
        }
        return "ReporteNuevo";
    }

    /* === PREVIEW: período en curso hasta hoy, según periodicidad === */
    @GetMapping("/preview")
    @PreAuthorize("hasAnyAuthority('Administrador','Contador')")
    public String preview(@RequestParam String tipo,
                          @RequestParam String periodicidad,
                          @RequestParam(required = false) Long idCuenta,
                          @RequestParam(name = "anterior", defaultValue = "false") boolean anterior,
                          Model model) {

        // Si anterior=true => último período CERRADO; si no, período EN CURSO hasta hoy
        LocalDate ini, fin;
        if (anterior) {
            var p = Periodo.ultimoCerrado(periodicidad); // PeriodoCalc
            ini = p.inicio();
            fin = p.fin();
        } else {
            var r = Periodo.enCursoHoy(periodicidad);    // Rango
            ini = r.inicio();
            fin = r.fin();
        }

        switch (tipo.toUpperCase()) {
            case "BALANZA" -> {
                model.addAllAttributes(balanzaService.consultar(ini, fin));
                return "BalanzaComprobacion :: tabla";
            }
            case "DIARIO" -> {
                model.addAllAttributes(libroDiarioService.consultar(ini, fin));
                return "RegistroLibroDiario :: tabla";
            }
            default -> throw new IllegalArgumentException("Tipo no soportado: " + tipo);
        }
    }

    /* === CREAR: último período cerrado === */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('Administrador','Contador')")
    public String crear(@RequestParam String tipo,
                        @RequestParam String periodicidad,
                        @RequestParam(required=false) String parametrosJson,
                        @RequestParam(required=false) String comentario,
                        RedirectAttributes ra) {
        try {
            Periodo.PeriodoCalc p = Periodo.ultimoCerrado(periodicidad);
            service.crearPendiente(
                    tipo, periodicidad, p.clave(), p.inicio(), p.fin(),
                    (parametrosJson==null? "{}" : parametrosJson),
                    comentario
            );
            ra.addFlashAttribute("ok",
                    "Reporte creado para el período " + p.clave() +
                            " y enviado al auditor para su revisión.");
            return "redirect:/reportes/mis";
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("err", "Ya existe un reporte para ese período.");
            return "redirect:/reportes/nuevo";
        }
    }

    // === GUARDAR CAMBIOS DE UN RECHAZADO ===
    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('Administrador','Contador')")
    public String editar(@PathVariable Long id,
                         @RequestParam(required=false) String comentario,
                         RedirectAttributes ra) {
        try {
            service.editarSiNoAprobado(id, comentario);
            ra.addFlashAttribute("ok",
                    "Cambios guardados y reporte reenviado al auditor.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/reportes/mis";
    }

    // Aprobar / Rechazar: SOLO Auditor
    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('Auditor')")
    public String aprobar(@PathVariable Long id){
        service.aprobar(id);
        return "redirect:/reportes";
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('Auditor')")
    public String rechazar(@PathVariable Long id, @RequestParam String motivo){
        service.rechazar(id, motivo);
        return "redirect:/reportes";
    }
}
