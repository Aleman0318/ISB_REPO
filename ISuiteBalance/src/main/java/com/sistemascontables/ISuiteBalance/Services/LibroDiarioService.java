package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.LibroDiarioDAO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibroDiarioService {

    private final DetallePartidaDAO detalleRepo;
    private final ContabilidadGeneracionService generacionSrv;

    public LibroDiarioService(DetallePartidaDAO detalleRepo,
                              ContabilidadGeneracionService generacionSrv) {
        this.detalleRepo = detalleRepo;
        this.generacionSrv = generacionSrv;
    }

    /**
     * 1) Sincroniza tbl_librodiario con las partidas existentes (rango si viene; de lo contrario, todas)
     * 2) Consulta filas para la vista usando tu proyección LibroDiarioDAO
     * 3) Calcula totales
     */
    public Map<String, Object> consultar(LocalDate desde, LocalDate hasta) {
        // 1) Asegurar persistencia en tbl_librodiario (si no hay rango, inserta todas las que falten)
        generacionSrv.generarLibroDiario(desde, hasta);

        // 2) Traer filas con tu query existente
        List<LibroDiarioDAO> filas = detalleRepo.libroDiario(desde, hasta);

        // 3) Totales
        BigDecimal totalDebe  = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;
        for (var r : filas) {
            if (r.getDebe()  != null) totalDebe  = totalDebe.add(r.getDebe());
            if (r.getHaber() != null) totalHaber = totalHaber.add(r.getHaber());
        }

        Map<String,Object> out = new HashMap<>();
        out.put("filas", filas);
        out.put("totalDebe", totalDebe);
        out.put("totalHaber", totalHaber);
        out.put("desdeStr", desde == null ? null : desde.toString());
        out.put("hastaStr", hasta == null ? null : hasta.toString());
        return out;
    }
}
