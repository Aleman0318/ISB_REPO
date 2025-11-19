// src/main/java/com/sistemascontables/ISuiteBalance/Services/BalanceGeneralService.java
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

    // Línea para mostrar en las tablas
    public static class LineaBG {
        public String codigo;
        public String nombre;
        public BigDecimal monto = BigDecimal.ZERO;
    }

    private final BalanzaComprobacionService balanzaService;
    private final EstadoFinancieroDAO estadoFinancieroDAO;
    private final BalanceGeneralDAO balanceDAO;

    public BalanceGeneralService(BalanzaComprobacionService balanzaService,
                                 EstadoFinancieroDAO estadoFinancieroDAO,
                                 BalanceGeneralDAO balanceDAO) {
        this.balanzaService = balanzaService;
        this.estadoFinancieroDAO = estadoFinancieroDAO;
        this.balanceDAO = balanceDAO;
    }

    // ================== CONSULTAR (para la vista) ==================
    public Map<String,Object> consultar(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy;

        // usamos tu propia balanza
        List<BalanzaComprobacionService.LineaBalanza> balanza =
                balanzaService.calcularBalanza(desde, hasta);

        List<LineaBG> activos = new ArrayList<>();
        List<LineaBG> pasivos = new ArrayList<>();
        List<LineaBG> patrimonio = new ArrayList<>();

        BigDecimal totalActivos = BigDecimal.ZERO;
        BigDecimal totalPasivoPatrimonio = BigDecimal.ZERO;

        for (var linea : balanza) {
            BigDecimal saldo = linea.getSaldoDeudor().subtract(linea.getSaldoAcreedor());
            String tipo = linea.getTipoCuenta();
            if (tipo == null) continue;

            String t = tipo.trim().toUpperCase();

            LineaBG f = new LineaBG();
            f.codigo = linea.getCodigo();
            f.nombre = linea.getNombreCuenta();
            f.monto = saldo;

            switch (t) {
                case "ACTIVO" -> {
                    activos.add(f);
                    totalActivos = totalActivos.add(saldo);
                }
                case "PASIVO" -> {
                    pasivos.add(f);
                    totalPasivoPatrimonio = totalPasivoPatrimonio.add(saldo);
                }
                case "PATRIMONIO" -> {
                    patrimonio.add(f);
                    totalPasivoPatrimonio = totalPasivoPatrimonio.add(saldo);
                }
                default -> {
                    // ingresos / gastos no se usan en el balance general
                }
            }
        }

        // Por ahora utilidad = 0 (luego la podemos enlazar al EERR si quieres)
        BigDecimal utilidad = BigDecimal.ZERO;
        BigDecimal totalPasivoPatrimonioMasUtilidad = totalPasivoPatrimonio.add(utilidad);
        BigDecimal diferencia = totalActivos.subtract(totalPasivoPatrimonioMasUtilidad);

        Map<String,Object> out = new HashMap<>();
        out.put("activos", activos);
        out.put("pasivos", pasivos);
        out.put("patrimonio", patrimonio);
        out.put("totalActivos", totalActivos);
        out.put("totalPasivosPatrimonio", totalPasivoPatrimonioMasUtilidad);
        out.put("utilidad", utilidad);
        out.put("diferencia", diferencia);
        out.put("desde", desde.toString());
        out.put("hasta", hasta.toString());
        return out;
    }

    // ================== GUARDAR (como los demás estados) ==================
    @Transactional
    public Long guardar(LocalDate desde, LocalDate hasta) {
        Map<String,Object> datos = consultar(desde, hasta);

        BigDecimal totalActivos = (BigDecimal) datos.get("totalActivos");
        BigDecimal totalPasivosPatrimonio = (BigDecimal) datos.get("totalPasivosPatrimonio");
        BigDecimal utilidad = (BigDecimal) datos.get("utilidad");

        String desdeStr = (String) datos.get("desde");
        String hastaStr = (String) datos.get("hasta");
        String periodo = desdeStr + ".." + hastaStr;
        String tipo = "BALANCE_GENERAL";

        // Cabecera en tbl_estado_financiero (igual que en balanza / EERR / flujo)
        var cab = estadoFinancieroDAO
                .findFirstByTipoEstadoAndPeriodo(tipo, periodo)
                .orElseGet(() -> {
                    var ef = new EstadoFinanciero();
                    ef.setTipoEstado(tipo);
                    ef.setPeriodo(periodo);
                    ef.setFechaGeneracion(LocalDateTime.now());
                    return estadoFinancieroDAO.save(ef);
                });

        // Detalle en tbl_balance
        var bg = new BalanceGeneral();
        bg.setEstado(cab);
        bg.setPeriodo(periodo);
        bg.setTotalActivos(totalActivos);
        bg.setTotalPasivos(totalPasivosPatrimonio);
        bg.setUtilidad(utilidad);

        balanceDAO.save(bg);

        return cab.getId();
    }
}
