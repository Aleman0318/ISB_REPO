package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Repositorios.PartidaResumen;
import com.sistemascontables.ISuiteBalance.Services.PartidaService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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

        // === ÚNICO CAMBIO: ordenar por idPartida DESC antes de mandar a la vista ===
        List<PartidaResumen> partidasOrdenadas = new java.util.ArrayList<>(pagina.getContent());
        partidasOrdenadas.sort((a, b) -> {
            Long ai = (a == null || a.getIdPartida() == null) ? Long.MIN_VALUE : a.getIdPartida().longValue();
            Long bi = (b == null || b.getIdPartida() == null) ? Long.MIN_VALUE : b.getIdPartida().longValue();
            return Long.compare(bi, ai); // DESC
        });

        model.addAttribute("pagina", pagina);
        model.addAttribute("partidas", partidasOrdenadas);   // ← usamos la lista ordenada
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "GestionPartida";
    }

    @GetMapping("/partidas/{id}/ver")
    public String ver(@PathVariable Integer id, Model model) {
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
