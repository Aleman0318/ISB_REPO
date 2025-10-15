package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.DetallePartida;
import com.sistemascontables.ISuiteBalance.Models.Partida;
import com.sistemascontables.ISuiteBalance.Services.PartidaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PartidasController {

    private final PartidaService service;

    public PartidasController(PartidaService service) {
        this.service = service;
    }

    @GetMapping("/partidas/nueva")
    public String nueva(Model model) {
        model.addAttribute("hoy", LocalDate.now());
        return "PartidasForm";
    }

    @PostMapping("/partidas")
    public String crear(
            @RequestParam("fecha") String fecha,
            @RequestParam("concepto") String concepto,
            @RequestParam("idUsuario") Integer idUsuario,
            @RequestParam("idCuenta[]") List<Integer> idCuentas,
            @RequestParam("debe[]") List<BigDecimal> debes,
            @RequestParam("haber[]") List<BigDecimal> haberes
    ) {
        Partida p = new Partida();
        p.setFecha(LocalDate.parse(fecha));
        p.setConcepto(concepto);
        p.setIdUsuario(idUsuario);

        List<DetallePartida> lineas = new ArrayList<>();
        for (int i = 0; i < idCuentas.size(); i++) {
            DetallePartida d = new DetallePartida();
            d.setIdCuenta(idCuentas.get(i));
            d.setMontoDebe(i < debes.size()  ? debes.get(i)  : BigDecimal.ZERO);
            d.setMontoHaber(i < haberes.size()? haberes.get(i): BigDecimal.ZERO);
            lineas.add(d);
        }

        service.crearPartida(p, lineas);
        return "redirect:/gestion-partida";
    }

    @GetMapping("/partidas/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("idPartida", id);
        // Para simplificar, el form se reutiliza y puedes precargar por JS si gustas
        return "PartidasForm";
    }

    @PostMapping("/partidas/{id}/actualizar")
    public String actualizar(@PathVariable Integer id,
                             @RequestParam("fecha") String fecha,
                             @RequestParam("concepto") String concepto,
                             @RequestParam("idUsuario") Integer idUsuario,
                             @RequestParam("idCuenta[]") List<Integer> idCuentas,
                             @RequestParam("debe[]") List<BigDecimal> debes,
                             @RequestParam("haber[]") List<BigDecimal> haberes) {

        Partida p = new Partida();
        p.setFecha(LocalDate.parse(fecha));
        p.setConcepto(concepto);
        p.setIdUsuario(idUsuario);

        List<DetallePartida> lineas = new ArrayList<>();
        for (int i = 0; i < idCuentas.size(); i++) {
            DetallePartida d = new DetallePartida();
            d.setIdCuenta(idCuentas.get(i));
            d.setMontoDebe(i < debes.size()  ? debes.get(i)  : BigDecimal.ZERO);
            d.setMontoHaber(i < haberes.size()? haberes.get(i): BigDecimal.ZERO);
            lineas.add(d);
        }

        service.actualizarPartida(id, p, lineas);
        return "redirect:/gestion-partida";
    }
}
