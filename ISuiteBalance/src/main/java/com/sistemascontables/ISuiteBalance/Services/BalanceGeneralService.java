package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.BalanceGeneral;
import com.sistemascontables.ISuiteBalance.Models.EstadoFinanciero;
import com.sistemascontables.ISuiteBalance.Repositorios.BalanceGeneralDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.EstadoFinancieroDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BalanceGeneralService {

    // ===== Fila que se muestra en la tabla HTML =====
    public static class LineaBG {
        public String codigo;
        public String nombre;
        public BigDecimal monto = BigDecimal.ZERO;
    }

    private final BalanzaComprobacionService balanzaService;
    private final EstadoFinancieroDAO estadoFinancieroDAO;
    private final BalanceGeneralDAO balanceDAO;
    private final EstadoResultadoService estadoResultadoService;

    public BalanceGeneralService(BalanzaComprobacionService balanzaService,
                                 EstadoFinancieroDAO estadoFinancieroDAO,
                                 BalanceGeneralDAO balanceDAO,
                                 EstadoResultadoService estadoResultadoService) {
        this.balanzaService = balanzaService;
        this.estadoFinancieroDAO = estadoFinancieroDAO;
        this.balanceDAO = balanceDAO;
        this.estadoResultadoService = estadoResultadoService;
    }

    // ======================================================
    // ===============   CONSULTAR PARA VISTA   =============
    // ======================================================
    public Map<String,Object> consultar(LocalDate desde, LocalDate hasta) {

        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        // Obtenemos la balanza resumida (con saldoDeudor y saldoAcreedor)
        List<BalanzaComprobacionService.LineaBalanza> balanza =
                balanzaService.calcularBalanza(desde, hasta);

        List<LineaBG> activos = new ArrayList<>();
        List<LineaBG> pasivos = new ArrayList<>();
        List<LineaBG> patrimonio = new ArrayList<>();

        BigDecimal totalActivos      = BigDecimal.ZERO;
        BigDecimal totalPasivos      = BigDecimal.ZERO;
        BigDecimal totalPatrimonio   = BigDecimal.ZERO;

        for (BalanzaComprobacionService.LineaBalanza linea : balanza) {

            // Tomamos siempre el saldo en POSITIVO (deudor o acreedor)
            BigDecimal saldo =
                    linea.getSaldoDeudor().signum() > 0
                            ? linea.getSaldoDeudor()
                            : linea.getSaldoAcreedor();

            String tipo = linea.getTipoCuenta();
            if (tipo == null) continue;
            String t = tipo.trim().toUpperCase(Locale.ROOT);

            LineaBG f = new LineaBG();
            f.codigo = linea.getCodigo();
            f.nombre = linea.getNombreCuenta();
            f.monto  = saldo;

            switch (t) {
                case "ACTIVO" -> {
                    activos.add(f);
                    totalActivos = totalActivos.add(saldo);
                }
                case "PASIVO" -> {
                    pasivos.add(f);
                    totalPasivos = totalPasivos.add(saldo);
                }
                case "PATRIMONIO" -> {
                    patrimonio.add(f);
                    totalPatrimonio = totalPatrimonio.add(saldo);
                }
                default -> {
                    // INGRESOS / GASTOS / COSTOS no se muestran en el Balance General
                }
            }
        }

        // 🔹 Utilidad REAL tomada del Estado de Resultado
        BigDecimal utilidad = estadoResultadoService.calcularUtilidad(desde, hasta);

        BigDecimal totalPasivosPatrimonio = totalPasivos
                .add(totalPatrimonio)
                .add(utilidad);

        BigDecimal diferencia = totalActivos.subtract(totalPasivosPatrimonio);

        Map<String,Object> out = new HashMap<>();
        out.put("activos", activos);
        out.put("pasivos", pasivos);
        out.put("patrimonio", patrimonio);

        out.put("totalActivos", totalActivos);
        out.put("totalPasivos", totalPasivos);
        out.put("totalPatrimonio", totalPatrimonio);
        out.put("totalPasivosPatrimonio", totalPasivosPatrimonio);
        out.put("utilidad", utilidad);
        out.put("diferencia", diferencia);

        out.put("desde", desde.toString());
        out.put("hasta", hasta.toString());

        return out;
    }

    // ======================================================
    // ===============   GUARDAR EN BD (SNAPSHOT) ===========
    // ======================================================
    @Transactional
    public Long guardar(LocalDate desde, LocalDate hasta) {

        Map<String,Object> datos = consultar(desde, hasta);

        BigDecimal totalActivos            = (BigDecimal) datos.get("totalActivos");
        BigDecimal totalPasivosPatrimonio  = (BigDecimal) datos.get("totalPasivosPatrimonio");
        BigDecimal utilidad                = (BigDecimal) datos.get("utilidad");

        String desdeStr = (String) datos.get("desde");
        String hastaStr = (String) datos.get("hasta");

        // Para la cabecera de EstadoFinanciero seguimos usando rango completo
        String periodoEf = desdeStr + ".." + hastaStr;
        String tipo      = "BALANCE_GENERAL";

        // Cabecera en tbl_estado_financiero (igual patrón que balanza, EERR, flujo)
        EstadoFinanciero cab = estadoFinancieroDAO
                .findFirstByTipoEstadoAndPeriodo(tipo, periodoEf)
                .orElseGet(() -> {
                    EstadoFinanciero ef = new EstadoFinanciero();
                    ef.setTipoEstado(tipo);
                    ef.setPeriodo(periodoEf);
                    ef.setFechaGeneracion(LocalDateTime.now());
                    return estadoFinancieroDAO.save(ef);
                });

        // Detalle en tbl_balance
        BalanceGeneral bg = new BalanceGeneral();
        bg.setEstado(cab);

        // 🔹 En tbl_balance.periodo solo guardamos la fecha final (corta)
        String periodoBalance = hastaStr;            // "2025-11-30" (10 chars)
        if (periodoBalance != null && periodoBalance.length() > 20) {
            periodoBalance = periodoBalance.substring(0, 20);
        }
        bg.setPeriodo(periodoBalance);

        bg.setTotalActivos(totalActivos);
        bg.setTotalPasivos(totalPasivosPatrimonio);
        bg.setUtilidad(utilidad);

        balanceDAO.save(bg);

        return cab.getId();
    }
}
