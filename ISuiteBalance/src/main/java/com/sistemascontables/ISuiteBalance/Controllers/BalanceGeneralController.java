// src/main/java/com/sistemascontables/ISuiteBalance/Controllers/BalanceGeneralController.java
package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.Empresa;
import com.sistemascontables.ISuiteBalance.Repositorios.EmpresaDAO;
import com.sistemascontables.ISuiteBalance.Services.BalanceGeneralService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class BalanceGeneralController {

    private final BalanceGeneralService service;
    private final EmpresaDAO empresaDAO;

    public BalanceGeneralController(BalanceGeneralService service,
                                    EmpresaDAO empresaDAO) {
        this.service = service;
        this.empresaDAO = empresaDAO;
    }

    private static LocalDate p(String s){
        if (s==null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch(Exception ignore){ return null; }
    }

    @GetMapping("/balance-general")
    public String ver(@RequestParam(required=false) String desde,
                      @RequestParam(required=false) String hasta,
                      Model model) {

        LocalDate d = p(desde), h = p(hasta);
        Map<String,Object> datos = service.consultar(d,h);
        model.addAllAttributes(datos);

        // Empresa por defecto: primer registro de tbl_empresa
        List<Empresa> empresas = empresaDAO.findAll();
        Empresa emp = empresas.isEmpty() ? null : empresas.get(0);
        model.addAttribute("empresa", emp);

        return "BalanceGeneral";  // nombre de tu plantilla Thymeleaf
    }

    @PostMapping("/balance-general/guardar")
    public String guardar(@RequestParam(required=false) String desde,
                          @RequestParam(required=false) String hasta,
                          RedirectAttributes ra) {

        LocalDate d = p(desde), h = p(hasta);

        String qs = "";
        if (d != null) qs += (qs.isEmpty()? "?":"&") + "desde=" + d;
        if (h != null) qs += (qs.isEmpty()? "?":"&") + "hasta=" + h;

        try{
            Long id = service.guardar(d,h);
            ra.addFlashAttribute("msgOk", "Balance general guardado (id_estado="+id+")");
        }catch(Exception e){
            ra.addFlashAttribute("msgErr", "No se pudo guardar: " + e.getMessage());
        }
        return "redirect:/balance-general" + qs;
    }
}
