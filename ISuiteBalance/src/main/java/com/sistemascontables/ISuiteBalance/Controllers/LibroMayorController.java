package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.LibroMayorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class LibroMayorController {

    private final LibroMayorService service;

    public LibroMayorController(LibroMayorService service) {
        this.service = service;
    }

    @GetMapping("/libro-mayor")
    public String libroMayor(
            @RequestParam(required = false) Long idCuenta,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            Model model
    ) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        model.addAllAttributes(service.consultar(idCuenta, d, h));
        return "LibroMayor"; // nombre del template
    }
}
