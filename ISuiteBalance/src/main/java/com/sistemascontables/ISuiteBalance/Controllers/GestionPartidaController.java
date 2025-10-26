package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Repositorios.PartidaResumen;
import com.sistemascontables.ISuiteBalance.Services.PartidaService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
public class GestionPartidaController {

    private final PartidaService service;

    public GestionPartidaController(PartidaService service) {
        this.service = service;
    }

    @GetMapping("/gestion-partida")
    public String listar(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {
        Page<PartidaResumen> pagina = service.listarResumen(page, size);
        model.addAttribute("pagina", pagina);
        model.addAttribute("partidas", pagina.getContent());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "GestionPartida";
    }

    @GetMapping("/partidas/{id}/ver")
    public String ver(@PathVariable Integer id, Model model) {
        model.addAttribute("idPartida", id);

        var lineas = service.obtenerLineasConNombre(id);

        // calcular totales
        java.math.BigDecimal totalDebe = lineas.stream()
                .map(l -> l.getMontoDebe() == null ? java.math.BigDecimal.ZERO : l.getMontoDebe())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalHaber = lineas.stream()
                .map(l -> l.getMontoHaber() == null ? java.math.BigDecimal.ZERO : l.getMontoHaber())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        model.addAttribute("lineas", lineas);
        model.addAttribute("totalDebe", totalDebe);
        model.addAttribute("totalHaber", totalHaber);

        return "DetallePartidas";
    }

    @PostMapping("/partidas/{id}/eliminar")
    public String eliminar(@PathVariable Integer id) {
        service.eliminarPartida(id);
        return "redirect:/gestion-partida";
    }

    @GetMapping("/partidas/{id}/ver-mayor")
    public String verDesdeMayor(@PathVariable Integer id, Model model) {
        model.addAttribute("idPartida", id);

        var lineas = service.obtenerLineasConNombre(id);

        BigDecimal totalDebe = lineas.stream()
                .map(l -> l.getMontoDebe() == null ? BigDecimal.ZERO : l.getMontoDebe())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHaber = lineas.stream()
                .map(l -> l.getMontoHaber() == null ? BigDecimal.ZERO : l.getMontoHaber())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("lineas", lineas);
        model.addAttribute("totalDebe", totalDebe);
        model.addAttribute("totalHaber", totalHaber);

        return "DetallePartidaMayor";
    }

}
