package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import com.sistemascontables.ISuiteBalance.Repositorios.CuentaContableDAO;
import com.sistemascontables.ISuiteBalance.Services.SaldosService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/saldos-iniciales")
public class SaldosInicialesController {

    private final CuentaContableDAO cuentaRepo;
    private final SaldosService saldos;

    public SaldosInicialesController(CuentaContableDAO cuentaRepo, SaldosService saldos) {
        this.cuentaRepo = cuentaRepo;
        this.saldos = saldos;
    }

    // Pantalla principal: listado de cuentas con campos editables de saldo inicial/fecha
    @GetMapping
    public String index(Model model) {
        List<CuentaContable> cuentas = cuentaRepo.findAllByOrderByCodigoAsc();
        model.addAttribute("cuentas", cuentas);
        return "/saldos-iniciales";
    }

    // Guardado masivo (la tabla envía arreglos idCuenta[], saldoInicial[], fechaSaldoInicial[])
    @PostMapping("/guardar")
    public String guardarMasivo(
            @RequestParam("idCuenta") List<Long> ids,
            @RequestParam("saldoInicial") List<BigDecimal> saldosIniciales,
            @RequestParam(value = "fechaSaldoInicial", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> fechas
    ) {
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            BigDecimal si = i < saldosIniciales.size() ? saldosIniciales.get(i) : BigDecimal.ZERO;
            LocalDate f = (fechas != null && i < fechas.size()) ? fechas.get(i) : null;
            saldos.actualizarSaldoInicial(id, si, f);
        }
        return "redirect:/saldos-iniciales?ok";
    }

    // Actualización por fila (uso alternativo si prefieres enviar uno por uno)
    @PostMapping("/{id}/fila")
    public String guardarFila(
            @PathVariable("id") Long idCuenta,
            @RequestParam("saldoInicial") BigDecimal saldoInicial,
            @RequestParam(value = "fechaSaldoInicial", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSaldoInicial
    ) {
        saldos.actualizarSaldoInicial(idCuenta, saldoInicial, fechaSaldoInicial);
        return "redirect:/saldos-iniciales?ok";
    }
}
