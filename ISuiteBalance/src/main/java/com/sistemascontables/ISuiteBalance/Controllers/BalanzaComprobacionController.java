package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.BalanzaComprobacionService;
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

    private final BalanzaComprobacionService service;

    public BalanzaComprobacionController(BalanzaComprobacionService service) {
        this.service = service;
    }

    @GetMapping("/balanza-comprobacion")
    public String ver(@RequestParam(required = false) String desde,
                      @RequestParam(required = false) String hasta,
                      Model model) {

        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        if (d == null && h == null) {
            Map<String,Object> datos = service.consultar(null, null);
            model.addAllAttributes(datos);
            model.addAttribute("desde", null);
            model.addAttribute("hasta", null);
        } else {
            Map<String,Object> datos = service.consultar(d, h);
            model.addAllAttributes(datos);
            model.addAttribute("desde", d != null ? d.toString() : null);
            model.addAttribute("hasta", h != null ? h.toString() : null);
        }
        return "BalanzaComprobacion";
    }

    @PostMapping("/balanza-comprobacion/guardar")
    public String guardar(@RequestParam(required = false) String desde,
                          @RequestParam(required = false) String hasta,
                          RedirectAttributes ra) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        Long idEstado = service.guardarSnapshot(d, h);
        ra.addFlashAttribute("msgOk", "Balanza guardada correctamente. id_estado = " + idEstado);

        String qs = "";
        if (d != null) qs += (qs.isEmpty() ? "?" : "&") + "desde=" + d;
        if (h != null) qs += (qs.isEmpty() ? "?" : "&") + "hasta=" + h;
        return "redirect:/balanza-comprobacion" + qs;
    }
}
