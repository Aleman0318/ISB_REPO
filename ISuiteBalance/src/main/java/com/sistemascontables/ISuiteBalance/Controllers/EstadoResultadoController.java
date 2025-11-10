// controllers/EstadoResultadoController.java
package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.EstadoResultadoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class EstadoResultadoController {

    private final EstadoResultadoService service;

    public EstadoResultadoController(EstadoResultadoService service) { this.service = service; }

    @GetMapping("/estado-resultado")
    public String ver(@RequestParam(required=false) String desde,
                      @RequestParam(required=false) String hasta,
                      Model model){
        LocalDate d = (desde==null||desde.isBlank())? null : LocalDate.parse(desde);
        LocalDate h = (hasta==null||hasta.isBlank())? null : LocalDate.parse(hasta);

        Map<String,Object> datos = service.consultar(d, h);
        model.addAllAttributes(datos);
        model.addAttribute("desde", d != null ? d.toString() : null);
        model.addAttribute("hasta", h != null ? h.toString() : null);
        return "EstadoResultado";
    }

    @PostMapping("/estado-resultado/guardar")
    public String guardar(@RequestParam(required=false) String desde,
                          @RequestParam(required=false) String hasta,
                          RedirectAttributes ra){
        LocalDate d = (desde==null||desde.isBlank())? null : LocalDate.parse(desde);
        LocalDate h = (hasta==null||hasta.isBlank())? null : LocalDate.parse(hasta);

        String qs = "";
        if (d != null) qs += (qs.isEmpty()? "?":"&") + "desde=" + d;
        if (h != null) qs += (qs.isEmpty()? "?":"&") + "hasta=" + h;

        try{
            Long id = service.guardar(d,h);
            ra.addFlashAttribute("msgOk", "Estado de resultado guardado (id_estado="+id+")");
        }catch(Exception e){
            ra.addFlashAttribute("msgErr", "No se pudo guardar: " + e.getMessage());
        }
        return "redirect:/estado-resultado" + qs;
    }
}
