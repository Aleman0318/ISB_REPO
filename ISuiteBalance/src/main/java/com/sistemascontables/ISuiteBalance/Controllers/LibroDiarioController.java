package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.LibroDiarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

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
            d = (desde != null) ? LocalDate.parse(desde) : null;
            h = (hasta != null) ? LocalDate.parse(hasta) : null;
        }
        // Si NO hay filtros, d/h quedan en null -> se sincroniza y se muestra TODO

        Map<String,Object> data = libroDiarioService.consultar(d, h);

        // === ORDENAR 'filas' POR ID DESC (idPartida o id) ===
        Object filasObj = data.get("filas");
        if (filasObj instanceof List<?>) {
            List<?> filas = new ArrayList<>((List<?>) filasObj); // copia para no mutar
            filas.sort((a, b) -> {
                Long ai = extraerId(a);
                Long bi = extraerId(b);
                // Nulls al final; orden DESC (mayor a menor)
                if (ai == null && bi == null) return 0;
                if (ai == null) return 1;
                if (bi == null) return -1;
                return Long.compare(bi, ai);
            });
            data.put("filas", filas);
        }

        model.addAllAttributes(data);

        // Devolver los strings para re-hidratar los inputs
        model.addAttribute("fechaStr",   fecha);
        model.addAttribute("periodoStr", periodo);
        model.addAttribute("desdeStr",   desde);
        model.addAttribute("hastaStr",   hasta);

        return "RegistroLibroDiario";
    }

    /** Intenta obtener el ID como Long desde:
     *  1) Map: "idPartida" o "id"
     *  2) POJO: getIdPartida() o getId()
     */
    private static Long extraerId(Object row) {
        if (row == null) return null;

        // Caso SQL nativo: cada fila puede ser un Map<String,Object>
        if (row instanceof Map<?,?> m) {
            Object v = m.get("idPartida");
            if (v == null) v = m.get("id");
            return toLong(v);
        }

        // Caso DTO/Entidad: getters típicos
        try {
            Method m = row.getClass().getMethod("getIdPartida");
            Object v = m.invoke(row);
            return toLong(v);
        } catch (Exception ignored) {}

        try {
            Method m = row.getClass().getMethod("getId");
            Object v = m.invoke(row);
            return toLong(v);
        } catch (Exception ignored) {}

        return null;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}
