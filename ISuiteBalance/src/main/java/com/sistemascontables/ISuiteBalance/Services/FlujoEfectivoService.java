// services/FlujoEfectivoService.java
package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.EstadoFinanciero;
import com.sistemascontables.ISuiteBalance.Models.FlujoEfectivo;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoFinancieroDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.FlujoEfectivoAgg;
import com.sistemascontables.ISuiteBalance.Repositorios.FlujoEfectivoDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FlujoEfectivoService {

    private final FlujoEfectivoDAO dao;
    private final EstadoFinancieroDAO efDao;
    private final EstadoResultadoService erService;

    public FlujoEfectivoService(FlujoEfectivoDAO dao,
                                EstadoFinancieroDAO efDao,
                                EstadoResultadoService erService) {
        this.dao = dao;
        this.efDao = efDao;
        this.erService = erService;
    }

    private static BigDecimal nz(BigDecimal x){ return x==null? BigDecimal.ZERO : x; }

    public Map<String,Object> consultar(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        FlujoEfectivoAgg agg = dao.calcular(desde, hasta);

        BigDecimal util = nz((BigDecimal) erService.consultar(desde, hasta).get("utilidad"));
        BigDecimal dAC  = nz(agg.getDeltaActivosCorrientes());
        BigDecimal dPC  = nz(agg.getDeltaPasivosCorrientes());
        BigDecimal dANC = nz(agg.getDeltaActivosNoCorrientes());
        BigDecimal divid= nz(agg.getDividendos());
        BigDecimal cap  = nz(agg.getCambiosCapital());
        BigDecimal dep  = nz(agg.getGastoDepreciacion());

        // Indirecto:
        // Operación = Utilidad + Depreciación − ΔAC + ΔPC
        BigDecimal ope = util.add(dep).subtract(dAC).add(dPC);

        // Inversión = − ΔANC  (aumento de no corrientes = salida de efectivo)
        BigDecimal inv = dANC.negate();

        // Financiamiento = CambiosCapital − Dividendos
        BigDecimal fin = cap.subtract(divid);

        BigDecimal neto = ope.add(inv).add(fin);

        Map<String,Object> out = new HashMap<>();
        out.put("ope", ope);
        out.put("inv", inv);
        out.put("fin", fin);
        out.put("neto", neto);

        out.put("utilidad", util);
        out.put("dep", dep);
        out.put("deltaAC", dAC);
        out.put("deltaPC", dPC);
        out.put("deltaANC", dANC);
        out.put("cambiosCapital", cap);
        out.put("dividendos", divid);

        out.put("desde", desde.toString());
        out.put("hasta", hasta.toString());
        return out;
    }

    @Transactional
    public Long guardar(LocalDate desde, LocalDate hasta) {
        Map<String,Object> m = consultar(desde, hasta);

        String periodo = m.get("desde") + ".." + m.get("hasta");
        String tipo = "ESTADO_FLUJO_EFECTIVO";

        EstadoFinanciero cab = efDao.findFirstByTipoEstadoAndPeriodo(tipo, periodo)
                .orElseGet(() -> {
                    EstadoFinanciero ef = new EstadoFinanciero();
                    ef.setTipoEstado(tipo);
                    ef.setPeriodo(periodo);
                    ef.setFechaGeneracion(LocalDateTime.now());
                    return efDao.save(ef);
                });

        FlujoEfectivo fe = new FlujoEfectivo();
        fe.setEstado(cab);
        fe.setFlujoOperativo((BigDecimal) m.get("ope"));
        fe.setFlujoInversion((BigDecimal) m.get("inv"));
        fe.setFlujoFinanciamiento((BigDecimal) m.get("fin"));
        dao.save(fe);

        return cab.getId();
    }
}
