package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.LibroDiarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Controller
public class LibroDiarioController {

    private final LibroDiarioService libroDiarioService;

    public LibroDiarioController(LibroDiarioService libroDiarioService) {
        this.libroDiarioService = libroDiarioService;
    }

    @GetMapping("/registro-libro-diario")
    public String mostrar(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            Model model) {

        // normalizar: "" -> null
        fecha   = (fecha   != null && !fecha.isBlank())   ? fecha   : null;
        periodo = (periodo != null && !periodo.isBlank()) ? periodo : null;
        desde   = (desde   != null && !desde.isBlank())   ? desde   : null;
        hasta   = (hasta   != null && !hasta.isBlank())   ? hasta   : null;

        LocalDate d = null, h = null;

        if (fecha != null) {                     // 1) un día
            LocalDate f = LocalDate.parse(fecha);
            d = f; h = f;
            periodo = desde = hasta = null;
        } else if (periodo != null) {            // 2) un mes (yyyy-MM)
            YearMonth ym = YearMonth.parse(periodo);
            d = ym.atDay(1);
            h = ym.atEndOfMonth();
            desde = hasta = null;
        } else if (desde != null || hasta != null) {  // 3) rango
            d = (desde != null) ? LocalDate.parse(desde) : LocalDate.MIN;
            h = (hasta != null) ? LocalDate.parse(hasta) : LocalDate.MAX;
        }

        Map<String,Object> data = libroDiarioService.consultar(d, h);
        model.addAllAttributes(data);

        // Devolver los strings para re-hidratar los inputs
        model.addAttribute("fechaStr",   fecha);
        model.addAttribute("periodoStr", periodo);
        model.addAttribute("desdeStr",   desde);
        model.addAttribute("hastaStr",   hasta);

        return "RegistroLibroDiario";
    }

}
