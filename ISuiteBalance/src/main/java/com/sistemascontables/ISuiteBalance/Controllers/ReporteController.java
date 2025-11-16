package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.ReporteService;
import com.sistemascontables.ISuiteBalance.Services.BalanzaComprobacionService;
import com.sistemascontables.ISuiteBalance.Services.LibroDiarioService;
import com.sistemascontables.ISuiteBalance.Services.LibroMayorService;
import com.sistemascontables.ISuiteBalance.Models.Periodo;
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

    // Lista: SOLO Auditor
    @GetMapping
    @PreAuthorize("hasAuthority('Auditor')")
    public String lista(Model model){
        model.addAttribute("pendientes", service.listarPendientes());
        model.addAttribute("aprobados",  service.listarAprobados());
        model.addAttribute("rechazados", service.listarRechazados());
        return "ReportesLista";
    }

    // Form nuevo: Admin y Contador
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('Administrador','Contador')")
    public String nuevoForm() {
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
            ra.addFlashAttribute("ok", "Reporte creado para el período: " + p.clave());
            return "redirect:/reportes";
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("err", "Ya existe un reporte para ese período.");
            return "redirect:/reportes/nuevo";
        }
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

    // Editar/reenviar: Admin y Contador (si lo mantienes)
    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAnyAuthority('Administrador','Contador')")
    public String editar(@PathVariable Long id,
                         @RequestParam(required=false) String parametrosJson,
                         @RequestParam(required=false) String comentario) {
        service.editarSiNoAprobado(id, parametrosJson, comentario);
        return "redirect:/reportes";
    }
}
