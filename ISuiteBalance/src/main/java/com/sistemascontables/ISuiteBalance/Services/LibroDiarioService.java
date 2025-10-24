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

    public LibroDiarioService(DetallePartidaDAO detalleRepo) {
        this.detalleRepo = detalleRepo;
    }

    public Map<String, Object> consultar(LocalDate desde, LocalDate hasta) {
        List<LibroDiarioDAO> filas = detalleRepo.libroDiario(desde, hasta);

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
