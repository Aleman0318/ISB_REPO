package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.EstadoFinanciero;
import com.sistemascontables.ISuiteBalance.Models.EstadoResultado;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoFinancieroDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoResultadoAgg;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoResultadoDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class EstadoResultadoService {

    private final EstadoResultadoDAO dao;
    private final EstadoFinancieroDAO efDao;

    public EstadoResultadoService(EstadoResultadoDAO dao, EstadoFinancieroDAO efDao) {
        this.dao = dao;
        this.efDao = efDao;
    }

    private static BigDecimal nz(BigDecimal x){ return x==null ? BigDecimal.ZERO : x; }

    public Map<String,Object> consultar(LocalDate desde, LocalDate hasta){
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        EstadoResultadoAgg agg = dao.calcular(desde, hasta);

        BigDecimal ingresos = nz(agg.getIngresos());
        BigDecimal costos   = nz(agg.getCostos());
        BigDecimal gastos   = nz(agg.getGastos());
        BigDecimal utilidad = ingresos.subtract(costos).subtract(gastos);

        Map<String,Object> out = new HashMap<>();
        out.put("ingresos", ingresos);
        out.put("costos", costos);
        out.put("gastos", gastos);
        out.put("utilidad", utilidad);
        out.put("desde", desde.toString());
        out.put("hasta", hasta.toString());
        return out;
    }

    /**
     * Devuelve solo la utilidad neta para un rango de fechas,
     * reutilizando la misma lógica de consultar().
     */
    public BigDecimal calcularUtilidad(LocalDate desde, LocalDate hasta) {
        Map<String,Object> m = consultar(desde, hasta);
        Object val = m.get("utilidad");

        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    @Transactional
    public Long guardar(LocalDate desde, LocalDate hasta){
        Map<String,Object> m = consultar(desde, hasta);
        BigDecimal ingresos = (BigDecimal) m.get("ingresos");
        BigDecimal costos   = (BigDecimal) m.get("costos");
        BigDecimal gastos   = (BigDecimal) m.get("gastos");
        BigDecimal utilidad = (BigDecimal) m.get("utilidad");

        String periodo = m.get("desde") + ".." + m.get("hasta");
        String tipo = "ESTADO_RESULTADO";

        EstadoFinanciero cab = efDao.findFirstByTipoEstadoAndPeriodo(tipo, periodo)
                .orElseGet(() -> {
                    EstadoFinanciero ef = new EstadoFinanciero();
                    ef.setTipoEstado(tipo);
                    ef.setPeriodo(periodo);
                    ef.setFechaGeneracion(LocalDateTime.now());
                    return efDao.save(ef);
                });

        EstadoResultado er = new EstadoResultado();
        er.setEstado(cab);
        er.setIngresos(ingresos);
        er.setCostos(costos);
        er.setGastos(gastos);
        er.setUtilidadNeta(utilidad);
        dao.save(er);

        return cab.getId();
    }
}
