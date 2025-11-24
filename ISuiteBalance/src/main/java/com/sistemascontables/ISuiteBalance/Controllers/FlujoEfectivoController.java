// controllers/FlujoEfectivoController.java
package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.FlujoEfectivoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class FlujoEfectivoController {

    private static LocalDate p(String s){
        if (s==null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch(Exception ignore){ return null; }
    }

    private final FlujoEfectivoService service;

    public FlujoEfectivoController(FlujoEfectivoService service) {
        this.service = service;
    }

    @GetMapping("/estado-flujo")
    public String ver(@RequestParam(required=false) String desde,
                      @RequestParam(required=false) String hasta,
                      Model model) {
        LocalDate d = p(desde), h = p(hasta);
        Map<String,Object> datos = service.consultar(d,h);
        model.addAllAttributes(datos);
        return "EstadoFlujo";
    }

    @PostMapping("/estado-flujo/guardar")
    public String guardar(@RequestParam(required=false) String desde,
                          @RequestParam(required=false) String hasta,
                          RedirectAttributes ra) {
        LocalDate d = p(desde), h = p(hasta);
        try {
            Long id = service.guardar(d,h);
            ra.addFlashAttribute("msgOk","Flujo de efectivo guardado (id_estado="+id+")");
        } catch(Exception e){
            ra.addFlashAttribute("msgErr","No se pudo guardar: "+e.getMessage());
        }
        String qs = "";
        if (d!=null) qs += (qs.isEmpty()?"?":"&")+"desde="+d;
        if (h!=null) qs += (qs.isEmpty()?"?":"&")+"hasta="+h;
        return "redirect:/estado-flujo"+qs;
    }
}
