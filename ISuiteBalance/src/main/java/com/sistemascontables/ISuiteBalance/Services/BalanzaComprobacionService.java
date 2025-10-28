package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.EstadoComprobacion;
import com.sistemascontables.ISuiteBalance.Models.EstadoFinanciero;
import com.sistemascontables.ISuiteBalance.Repositorios.Balanza;
import com.sistemascontables.ISuiteBalance.Repositorios.BalanzaComprobacionDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoComprobacionDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoFinancieroDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BalanzaComprobacionService {

    private static final Logger log = LoggerFactory.getLogger(BalanzaComprobacionService.class);

    private final BalanzaComprobacionDAO dao;              // consulta agregada
    private final EstadoFinancieroDAO estadoFinancieroDAO; // cabecera/pivote
    private final EstadoComprobacionDAO estadoComprobacionDAO; // totales

    public BalanzaComprobacionService(BalanzaComprobacionDAO dao,
                                      EstadoFinancieroDAO estadoFinancieroDAO,
                                      EstadoComprobacionDAO estadoComprobacionDAO) {
        this.dao = dao;
        this.estadoFinancieroDAO = estadoFinancieroDAO;
        this.estadoComprobacionDAO = estadoComprobacionDAO;
    }

    public static class Fila {
        public String codigo;
        public String nombre;
        public BigDecimal saldoInicial = BigDecimal.ZERO;
        public BigDecimal debitos      = BigDecimal.ZERO;
        public BigDecimal creditos     = BigDecimal.ZERO;
        public BigDecimal saldoFinal   = BigDecimal.ZERO;
    }

    public Map<String,Object> consultar(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        List<Balanza> agg = dao.calcularBalanzaCompleta(desde, hasta);

        log.info("[BALANZA] rango {}..{} -> {} filas: {}",
                desde, hasta, agg.size(),
                agg.stream().map(Balanza::getCodigo).toList());

        List<Fila> filas = new ArrayList<>();
        BigDecimal totalSI = BigDecimal.ZERO, totalDeb = BigDecimal.ZERO,
                totalHab = BigDecimal.ZERO, totalDeud = BigDecimal.ZERO,
                totalAcr = BigDecimal.ZERO;

        for (Balanza a : agg) {
            BigDecimal si  = nvl(a.getSaldoInicial());
            BigDecimal deb = nvl(a.getDebitos());
            BigDecimal hab = nvl(a.getCreditos());
            BigDecimal sf  = si.add(deb).subtract(hab);

            Fila f = new Fila();
            f.codigo = a.getCodigo();
            f.nombre = a.getNombre();
            f.saldoInicial = si;
            f.debitos      = deb;
            f.creditos     = hab;
            f.saldoFinal   = sf;
            filas.add(f);

            totalSI  = totalSI.add(si);
            totalDeb = totalDeb.add(deb);
            totalHab = totalHab.add(hab);
            if (sf.signum() >= 0) totalDeud = totalDeud.add(sf);
            else                   totalAcr  = totalAcr.add(sf.negate());
        }

        Map<String,Object> out = new HashMap<>();
        out.put("filas", filas);
        out.put("totalSaldoInicial", totalSI);
        out.put("totalDebitos",      totalDeb);
        out.put("totalCreditos",     totalHab);
        out.put("totalFinalDeudor",  totalDeud);
        out.put("totalFinalAcreedor",totalAcr);
        out.put("validacionOk",      totalDeb.compareTo(totalHab) == 0);
        out.put("desde", desde.toString());
        out.put("hasta", hasta.toString());
        return out;
    }

    private static BigDecimal nvl(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }

    /**
     * Persiste un snapshot de la balanza en:
     * - tbl_estadofinanciero (cabecera) [tipo=BALANZA_COMPROBACION, periodo=desde..hasta]
     * - tbl_estadocomprobacion (totales debe/haber vinculados a id_estado)
     * Devuelve el id_estado (cabecera).
     */
    @Transactional
    public Long guardarSnapshot(LocalDate desde, LocalDate hasta) {
        Map<String,Object> datos = consultar(desde, hasta);
        BigDecimal totalDeb = (BigDecimal) datos.get("totalDebitos");
        BigDecimal totalHab = (BigDecimal) datos.get("totalCreditos");

        String periodo = datos.get("desde") + ".." + datos.get("hasta");
        String tipo    = "BALANZA_COMPROBACION";

        // Reutiliza si ya existe cabecera para mismo periodo+tipo, si no crea
        EstadoFinanciero cab = estadoFinancieroDAO
                .findFirstByTipoEstadoAndPeriodo(tipo, periodo)
                .orElseGet(() -> {
                    EstadoFinanciero ef = new EstadoFinanciero();
                    ef.setTipoEstado(tipo);
                    ef.setPeriodo(periodo);
                    ef.setFechaGeneracion(LocalDateTime.now());
                    return estadoFinancieroDAO.save(ef);
                });

        // Guarda totales (si prefieres actualizar el último, aquí podrías buscar/borrar antes)
        EstadoComprobacion ec = new EstadoComprobacion();
        ec.setEstado(cab);
        ec.setTotalDebe(totalDeb);
        ec.setTotalHaber(totalHab);
        estadoComprobacionDAO.save(ec);

        log.info("[BALANZA][GUARDAR] {} -> Debe={} Haber={}", periodo, totalDeb, totalHab);
        return cab.getId();
    }
}
