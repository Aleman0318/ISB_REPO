// services/VariacionPatrimonioService.java
package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.EstadoFinanciero;
import com.sistemascontables.ISuiteBalance.Models.VariacionPatrimonio;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoFinancieroDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.VariacionPatrimonioAgg;
import com.sistemascontables.ISuiteBalance.Repositorios.VariacionPatrimonioDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class VariacionPatrimonioService {

    private final VariacionPatrimonioDAO dao;
    private final EstadoFinancieroDAO efDao;
    private final EstadoResultadoService erService;

    public VariacionPatrimonioService(VariacionPatrimonioDAO dao,
                                      EstadoFinancieroDAO efDao,
                                      EstadoResultadoService erService) {
        this.dao = dao;
        this.efDao = efDao;
        this.erService = erService;
    }

    private static BigDecimal nz(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }

    public Map<String, Object> consultar(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        // Cambios de capital + dividendos desde el mayor
        VariacionPatrimonioAgg agg = dao.calcular(desde, hasta);
        BigDecimal cambiosCapital = nz(agg.getCambiosCapital());
        BigDecimal dividendos     = nz(agg.getDividendos());

        // Utilidad retenida del periodo = utilidad neta del Estado de Resultado
        BigDecimal utilidadPeriodo = (BigDecimal) erService.consultar(desde, hasta).get("utilidad");
        utilidadPeriodo = nz(utilidadPeriodo);

        // Variación neta sugerida (para KPI)
        BigDecimal variacionNeta = cambiosCapital.subtract(dividendos).add(utilidadPeriodo);

        Map<String, Object> out = new HashMap<>();
        out.put("cambiosCapital", cambiosCapital);
        out.put("dividendos", dividendos);
        out.put("utilidadRetenida", utilidadPeriodo);
        out.put("variacionNeta", variacionNeta);
        out.put("desde", desde.toString());
        out.put("hasta", hasta.toString());
        return out;
    }

    @Transactional
    public Long guardar(LocalDate desde, LocalDate hasta) {
        Map<String, Object> m = consultar(desde, hasta);

        String periodo = m.get("desde") + ".." + m.get("hasta");
        String tipo = "ESTADO_VARIACIONES_PATRIMONIO";

        EstadoFinanciero cab = efDao.findFirstByTipoEstadoAndPeriodo(tipo, periodo)
                .orElseGet(() -> {
                    EstadoFinanciero ef = new EstadoFinanciero();
                    ef.setTipoEstado(tipo);
                    ef.setPeriodo(periodo);
                    ef.setFechaGeneracion(LocalDateTime.now());
                    return efDao.save(ef);
                });

        VariacionPatrimonio vp = new VariacionPatrimonio();
        vp.setEstado(cab);
        vp.setCambiosCapital((BigDecimal) m.get("cambiosCapital"));
        vp.setDividendos((BigDecimal) m.get("dividendos"));
        vp.setUtilidadRetenida((BigDecimal) m.get("utilidadRetenida"));
        dao.save(vp);

        return cab.getId();
    }
}
