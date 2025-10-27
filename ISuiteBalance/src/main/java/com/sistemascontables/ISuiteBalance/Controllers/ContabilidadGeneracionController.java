package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.ContabilidadGeneracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ContabilidadGeneracionController {

    private final ContabilidadGeneracionService service;

    public ContabilidadGeneracionController(ContabilidadGeneracionService service) {
        this.service = service;
    }

    @GetMapping("/conta/generar/diario")
    public ResponseEntity<?> generarDiario(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        int ins = service.generarLibroDiario(d, h);
        Map<String, Object> out = new HashMap<>();
        out.put("insertados", ins);
        out.put("desde", d);
        out.put("hasta", h);
        return ResponseEntity.ok(out);
    }

    // >>> NUEVO: persistir Libro Mayor del periodo (mes) detectado
    @GetMapping("/conta/generar/mayor")
    public ResponseEntity<?> generarMayor(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        var res = service.recalcularLibroMayorPeriodo(d, h);

        Map<String, Object> out = new HashMap<>();
        out.put("periodo", res.periodo());
        out.put("rangoDesde", res.rangoDesde());
        out.put("rangoHasta", res.rangoHasta());
        out.put("borrados", res.borrados());
        out.put("insertados", res.insertados());
        return ResponseEntity.ok(out);
    }

    // Opcional: ambos
    @GetMapping("/conta/generar/todo")
    public ResponseEntity<?> generarTodo(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        int insDiario = service.generarLibroDiario(d, h);
        var resMayor  = service.recalcularLibroMayorPeriodo(d, h);

        Map<String, Object> out = new HashMap<>();
        out.put("diarioInsertados", insDiario);
        out.put("mayorPeriodo", resMayor.periodo());
        out.put("mayorRangoDesde", resMayor.rangoDesde());
        out.put("mayorRangoHasta", resMayor.rangoHasta());
        out.put("mayorBorrados", resMayor.borrados());
        out.put("mayorInsertados", resMayor.insertados());
        return ResponseEntity.ok(out);
    }
}
