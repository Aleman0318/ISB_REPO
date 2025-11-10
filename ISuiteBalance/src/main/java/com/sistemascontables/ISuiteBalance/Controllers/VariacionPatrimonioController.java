package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.VariacionPatrimonioService;
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
public class VariacionPatrimonioController {

    private static final Logger log = LoggerFactory.getLogger(VariacionPatrimonioController.class);
    private final VariacionPatrimonioService service;

    public VariacionPatrimonioController(VariacionPatrimonioService service) {
        this.service = service;
    }

    /** Parser ISO yyyy-MM-dd tolerante a comas y vacíos. */
    private static LocalDate parseDateOrNull(String raw) {
        if (raw == null) return null;
        String first = raw.split(",")[0].trim(); // "2025-11-09," -> "2025-11-09"
        if (first.isEmpty()) return null;
        try { return LocalDate.parse(first); } catch (Exception ex) { return null; }
    }

    @GetMapping("/estado-variaciones")
    public String ver(@RequestParam(required = false) String desde,
                      @RequestParam(required = false) String hasta,
                      Model model) {

        log.debug("[VARIACIONES][GET] raw desde='{}' hasta='{}'", desde, hasta);

        LocalDate d = parseDateOrNull(desde);
        LocalDate h = parseDateOrNull(hasta);

        Map<String,Object> datos = service.consultar(d, h);
        model.addAllAttributes(datos);
        model.addAttribute("desde", d != null ? d.toString() : null);
        model.addAttribute("hasta", h != null ? h.toString() : null);

        return "EstadoVariaciones"; // <-- Debe existir templates/EstadoVariaciones.html (misma mayúscula/minúscula)
    }

    @PostMapping("/estado-variaciones/guardar")
    public String guardar(@RequestParam(required = false) String desde,
                          @RequestParam(required = false) String hasta,
                          RedirectAttributes ra) {

        log.debug("[VARIACIONES][POST] raw desde='{}' hasta='{}'", desde, hasta);

        LocalDate d = parseDateOrNull(desde);
        LocalDate h = parseDateOrNull(hasta);

        StringBuilder qs = new StringBuilder();
        if (d != null) qs.append(qs.length()==0? "?":"&").append("desde=").append(d);
        if (h != null) qs.append(qs.length()==0? "?":"&").append("hasta=").append(h);

        try {
            Long id = service.guardar(d, h);
            ra.addFlashAttribute("msgOk", "Estado de variaciones guardado (id_estado=" + id + ")");
            log.info("[VARIACIONES][OK] Snapshot guardado id_estado={}", id);
        } catch (Exception e) {
            ra.addFlashAttribute("msgErr", "No se pudo guardar: " + e.getMessage());
            log.error("[VARIACIONES][ERR] No se pudo guardar snapshot", e);
        }

        return "redirect:/estado-variaciones" + qs;
    }
}
