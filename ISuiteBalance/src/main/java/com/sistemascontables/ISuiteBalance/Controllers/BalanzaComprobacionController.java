package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.BalanzaComprobacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            // PRIMERA CARGA: calculas con tus defaults del service (ej. mes actual)
            Map<String,Object> datos = service.consultar(null, null);
            model.addAllAttributes(datos);

            // pero dejas los inputs vacíos en la vista
            model.addAttribute("desde", null);
            model.addAttribute("hasta", null);
        } else {
            // FILTRO APLICADO: calculas con el rango indicado
            Map<String,Object> datos = service.consultar(d, h);
            model.addAllAttributes(datos);

            // y reflejas los valores en los inputs
            model.addAttribute("desde", d != null ? d.toString() : null);
            model.addAttribute("hasta", h != null ? h.toString() : null);
        }
        return "BalanzaComprobacion";
    }
}
