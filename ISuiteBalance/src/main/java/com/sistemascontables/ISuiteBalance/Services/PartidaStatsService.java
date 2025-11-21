package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Repositorios.PartidaStatsDAO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Servicio para armar los datos del gráfico de "Reporte de Partidas"
 * (últimos 5 meses).
 */
@Service
public class PartidaStatsService {

    private final PartidaStatsDAO partidaStatsDAO;

    public PartidaStatsService(PartidaStatsDAO partidaStatsDAO) {
        this.partidaStatsDAO = partidaStatsDAO;
    }

    /**
     * Devuelve labels (meses) y valores (cantidad de partidas) para
     * los últimos 5 meses contando el mes actual.
     */
    public PartidasChartData obtenerUltimos5Meses() {
        int n = 5;

        // Primer día del mes actual
        LocalDate hoyPrimerDia = LocalDate.now().withDayOfMonth(1);
        // Mes más antiguo que vamos a mostrar (hace 4 meses)
        LocalDate desde = hoyPrimerDia.minusMonths(n - 1);

        // Consulta agrupada en BD desde ese mes
        List<Object[]> filas = partidaStatsDAO.contarPartidasPorMesDesde(desde);

        // Mapa: "YYYY-MM" -> total
        Map<String, Long> mapa = new HashMap<>();
        for (Object[] fila : filas) {
            String periodo = (String) fila[0]; // ejemplo "2025-03"
            Number total = (Number) fila[1];
            mapa.put(periodo, total.longValue());
        }

        DateTimeFormatter keyFmt   = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM", new Locale("es", "ES"));

        List<String> labels  = new ArrayList<>();
        List<Long>   valores = new ArrayList<>();

        // Recorremos mes por mes, rellenando con 0 si no hay datos
        for (int i = 0; i < n; i++) {
            LocalDate mes = desde.plusMonths(i);
            String key   = mes.format(keyFmt);                 // "2025-03"
            String label = labelFmt.format(mes).toUpperCase(); // "MAR", "ABR", etc.

            labels.add(label);
            valores.add(mapa.getOrDefault(key, 0L));
        }

        return new PartidasChartData(labels, valores);
    }

    /**
     * DTO simple para pasar labels + valores al controlador.
     */
    public static class PartidasChartData {
        private final List<String> labels;
        private final List<Long> valores;

        public PartidasChartData(List<String> labels, List<Long> valores) {
            this.labels = labels;
            this.valores = valores;
        }

        public List<String> getLabels() {
            return labels;
        }

        public List<Long> getValores() {
            return valores;
        }
    }
}
