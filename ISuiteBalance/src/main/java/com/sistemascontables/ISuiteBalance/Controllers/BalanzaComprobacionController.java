package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.BalanzaComprobacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class BalanzaComprobacionController {

    private static final Logger log = LoggerFactory.getLogger(BalanzaComprobacionController.class);

    private final BalanzaComprobacionService service;

    public BalanzaComprobacionController(BalanzaComprobacionService service) {
        this.service = service;
    }

    /**
     * Parser “tolerante”:
     * - Acepta null/vacío y devuelve null.
     * - Ignora cualquier cosa después de una coma.
     * - Solo parsea si queda con forma yyyy-MM-dd; si no, devuelve null.
     */
    private static LocalDate parseDateOrNull(String raw) {
        if (raw == null) return null;
        String first = raw.split(",")[0].trim();  // "2025-11-08,..." -> "2025-11-08"
        if (first.isEmpty()) return null;
        // Validar patrón ISO simple para evitar DateTimeParseException
        if (!first.matches("\\d{4}-\\d{2}-\\d{2}")) return null;
        try {
            return LocalDate.parse(first);
        } catch (Exception ignore) {
            return null;
        }
    }

    @GetMapping("/balanza-comprobacion")
    public String ver(@RequestParam(required = false) String desde,
                      @RequestParam(required = false) String hasta,
                      Model model) {

        log.debug("[BALANZA][GET] raw desde='{}' hasta='{}'", desde, hasta);

        // Si no envías filtros en el HTML, normalmente llegarán null/vacíos y
        // el service usará sus defaults (primer día del mes .. hoy).
        LocalDate d = parseDateOrNull(desde);
        LocalDate h = parseDateOrNull(hasta);

        Map<String,Object> datos = service.consultar(d, h);
        model.addAllAttributes(datos);

        // Mantengo estas variables por si en el futuro vuelves a mostrar filtros
        model.addAttribute("desde", d != null ? d.toString() : null);
        model.addAttribute("hasta", h != null ? h.toString() : null);

        return "BalanzaComprobacion";
    }

    @PostMapping("/balanza-comprobacion/guardar")
    public String guardar(@RequestParam(required = false) String desde,
                          @RequestParam(required = false) String hasta,
                          RedirectAttributes ra) {

        log.debug("[BALANZA][POST] raw desde='{}' hasta='{}'", desde, hasta);

        LocalDate d = parseDateOrNull(desde);
        LocalDate h = parseDateOrNull(hasta);

        try {
            Long idEstado = service.guardarSnapshot(d, h);  // si d/h son null, el service usa defaults
            ra.addFlashAttribute("msgOk", "Balanza guardada correctamente. id_estado = " + idEstado);
            log.info("[BALANZA][OK] Snapshot guardado id_estado={}", idEstado);
        } catch (Exception e) {
            log.error("[BALANZA][ERR] No se pudo guardar el snapshot", e);
            ra.addFlashAttribute("msgErr", "No se pudo guardar la balanza: " + e.getMessage());
        }

        // No reinyecto querystring porque ya no usas filtros en la vista
        return "redirect:/balanza-comprobacion";
    }
}
