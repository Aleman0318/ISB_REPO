package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.ContabilidadGeneracionService;
import com.sistemascontables.ISuiteBalance.Services.LibroMayorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class LibroMayorController {

    private final LibroMayorService mayorService;
    private final ContabilidadGeneracionService contaGenService;

    public LibroMayorController(LibroMayorService mayorService,
                                ContabilidadGeneracionService contaGenService) {
        this.mayorService = mayorService;
        this.contaGenService = contaGenService;
    }

    @GetMapping("/libro-mayor")
    public String libroMayor(
            @RequestParam(required = false) Long idCuenta,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false, defaultValue = "true") Boolean persist,
            Model model
    ) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        if (Boolean.TRUE.equals(persist)) {
            var r = contaGenService.recalcularLibroMayorPeriodo(d, h);
            // Si quieres, muestra un pequeño aviso en UI
            model.addAttribute("persistInfoMayor",
                    "Resumen mayor " + r.periodo() + ": borrados=" + r.borrados() + ", insertados=" + r.insertados());
        }

        model.addAllAttributes(mayorService.consultar(idCuenta, d, h));
        return "LibroMayor";
    }
}
