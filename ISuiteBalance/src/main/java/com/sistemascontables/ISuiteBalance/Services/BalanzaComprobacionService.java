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

    private final BalanzaComprobacionDAO dao;
    private final EstadoFinancieroDAO estadoFinancieroDAO;
    private final EstadoComprobacionDAO estadoComprobacionDAO;

    public BalanzaComprobacionService(BalanzaComprobacionDAO dao,
                                      EstadoFinancieroDAO estadoFinancieroDAO,
                                      EstadoComprobacionDAO estadoComprobacionDAO) {
        this.dao = dao;
        this.estadoFinancieroDAO = estadoFinancieroDAO;
        this.estadoComprobacionDAO = estadoComprobacionDAO;
    }

    // === Fila usada en la vista de balanza de comprobación ===
    public static class Fila {
        public String codigo;
        public String nombre;
        public BigDecimal saldoInicial = BigDecimal.ZERO;
        public BigDecimal debitos      = BigDecimal.ZERO;
        public BigDecimal creditos     = BigDecimal.ZERO;
        public BigDecimal saldoFinal   = BigDecimal.ZERO;
    }

    // === DTO usado por el Balance General ===
    public static class LineaBalanza {
        private final String codigo;
        private final String nombreCuenta;
        private final String tipoCuenta;
        private final BigDecimal saldoDeudor;
        private final BigDecimal saldoAcreedor;

        public LineaBalanza(String codigo,
                            String nombreCuenta,
                            String tipoCuenta,
                            BigDecimal saldoDeudor,
                            BigDecimal saldoAcreedor) {
            this.codigo = codigo;
            this.nombreCuenta = nombreCuenta;
            this.tipoCuenta = tipoCuenta;
            this.saldoDeudor = saldoDeudor;
            this.saldoAcreedor = saldoAcreedor;
        }

        public String getCodigo() { return codigo; }
        public String getNombreCuenta() { return nombreCuenta; }
        public String getTipoCuenta() { return tipoCuenta; }
        public BigDecimal getSaldoDeudor() { return saldoDeudor; }
        public BigDecimal getSaldoAcreedor() { return saldoAcreedor; }
    }

    // ================= LÓGICA ORIGINAL: BALANZA PARA LA VISTA =================

    public Map<String,Object> consultar(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        // ✅ ventana semiabierta: [desde, hasta+1)
        LocalDate hastaExcl = hasta.plusDays(1);

        List<Balanza> agg = dao.calcularBalanzaCompleta(desde, hastaExcl);

        log.info("[BALANZA] rango {}..{} (excl {}) -> {} filas: {}",
                desde, hasta, hastaExcl, agg.size(),
                agg.stream().map(Balanza::getCodigo).toList());

        List<Fila> filas = new ArrayList<>();
        BigDecimal totalSI = BigDecimal.ZERO, totalDeb = BigDecimal.ZERO,
                totalHab = BigDecimal.ZERO, totalDeud = BigDecimal.ZERO,
                totalAcr = BigDecimal.ZERO;

        for (Balanza a : agg) {
            BigDecimal si  = nvl(a.getSaldoInicial());
            BigDecimal deb = nvl(a.getDebitos());
            BigDecimal hab = nvl(a.getCreditos());

            // saldo inicial con signo por naturaleza
            BigDecimal siSignado = esAcreedora(a.getTipoCuenta()) ? si.negate() : si;

            BigDecimal sf  = siSignado.add(deb).subtract(hab);

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

    private static boolean esAcreedora(String tipo) {
        if (tipo == null) return false;
        String t = tipo.trim().toUpperCase(Locale.ROOT);
        return t.equals("PASIVO") || t.equals("PATRIMONIO") || t.equals("INGRESO") || t.equals("INGRESOS");
    }

    @SuppressWarnings("unused")
    private static boolean esDeudora(String tipo) {
        if (tipo == null) return true;
        String t = tipo.trim().toUpperCase(Locale.ROOT);
        return t.equals("ACTIVO") || t.equals("GASTO") || t.equals("GASTOS") || t.equals("COSTO") || t.equals("COSTOS");
    }

    // ================= SNAPSHOT (GUARDAR EN BD) =================

    @Transactional
    public Long guardarSnapshot(LocalDate desde, LocalDate hasta) {
        Map<String,Object> datos = consultar(desde, hasta);
        BigDecimal totalDeb = (BigDecimal) datos.get("totalDebitos");
        BigDecimal totalHab = (BigDecimal) datos.get("totalCreditos");

        String periodo = datos.get("desde") + ".." + datos.get("hasta");
        String tipo    = "BALANZA_COMPROBACION";

        var cab = estadoFinancieroDAO
                .findFirstByTipoEstadoAndPeriodo(tipo, periodo)
                .orElseGet(() -> {
                    var ef = new EstadoFinanciero();
                    ef.setTipoEstado(tipo);
                    ef.setPeriodo(periodo);
                    ef.setFechaGeneracion(LocalDateTime.now());
                    return estadoFinancieroDAO.save(ef);
                });

        var ec = new EstadoComprobacion();
        ec.setEstado(cab);
        ec.setTotalDebe(totalDeb);
        ec.setTotalHaber(totalHab);
        estadoComprobacionDAO.save(ec);

        log.info("[BALANZA][GUARDAR] {} -> Debe={} Haber={}", periodo, totalDeb, totalHab);
        return cab.getId();
    }

    // ================= NUEVO: SALDOS PARA BALANCE GENERAL =================

    /**
     * Devuelve la lista de LineaBalanza (ya separando Deudor/Acreedor)
     * para el rango de fechas indicado.
     * Se basa en la misma query y lógica que consultar().
     */
    public List<LineaBalanza> calcularBalanza(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        LocalDate hastaExcl = hasta.plusDays(1);

        List<Balanza> agg = dao.calcularBalanzaCompleta(desde, hastaExcl);

        List<LineaBalanza> resultado = new ArrayList<>();

        for (Balanza a : agg) {
            BigDecimal si  = nvl(a.getSaldoInicial());
            BigDecimal deb = nvl(a.getDebitos());
            BigDecimal hab = nvl(a.getCreditos());

            BigDecimal siSignado = esAcreedora(a.getTipoCuenta()) ? si.negate() : si;
            BigDecimal sf  = siSignado.add(deb).subtract(hab);

            BigDecimal deudor   = BigDecimal.ZERO;
            BigDecimal acreedor = BigDecimal.ZERO;
            if (sf.signum() >= 0) {
                deudor = sf;
            } else {
                acreedor = sf.negate();
            }

            resultado.add(new LineaBalanza(
                    a.getCodigo(),
                    a.getNombre(),
                    a.getTipoCuenta(),
                    deudor,
                    acreedor
            ));
        }

        return resultado;
    }

    /**
     * Overload con idEmpresa para que compile con BalanceGeneralService.
     * Por ahora idEmpresa no se usa porque la query aún no filtra por empresa.
     * Más adelante, si tu tabla Balanza/BalanzaCompleta incluye empresa,
     * aquí se puede aprovechar para filtrar.
     */
    public List<LineaBalanza> calcularBalanza(Long idEmpresa, LocalDate desde, LocalDate hasta) {
        return calcularBalanza(desde, hasta);
    }
}
