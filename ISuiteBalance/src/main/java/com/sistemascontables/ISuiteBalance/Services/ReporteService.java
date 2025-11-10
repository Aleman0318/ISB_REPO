package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.Reporte;
import com.sistemascontables.ISuiteBalance.Repositorios.ReporteDAO;
import com.sistemascontables.ISuiteBalance.Models.Periodo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

@Service
public class ReporteService {

    private final ReporteDAO reporteDAO;
    private final BalanzaComprobacionService balanzaService;

    public ReporteService(ReporteDAO dao, BalanzaComprobacionService balanzaService){
        this.reporteDAO = dao;
        this.balanzaService = balanzaService;
    }

    /* ========= CREAR (último período cerrado) ========= */
    @Transactional
    public Reporte crearPendiente(String tipo, String periodicidad,
                                  String periodoClave,
                                  LocalDate inicio, LocalDate fin,
                                  String parametrosJson,
                                  String comentario) {

        reporteDAO.findByTipoReporteAndPeriodicidadAndPeriodoClave(tipo, periodicidad, periodoClave)
                .ifPresent(r -> { throw new IllegalStateException("Ya existe un reporte con esos datos"); });

        Map<String,Object> resumen = balanzaService.consultar(inicio, fin);
        BigDecimal debe  = (BigDecimal) resumen.getOrDefault("totalDebitos",  BigDecimal.ZERO);
        BigDecimal haber = (BigDecimal) resumen.getOrDefault("totalCreditos", BigDecimal.ZERO);

        Reporte r = new Reporte();
        r.setTipoReporte(tipo);
        r.setPeriodicidad(periodicidad);
        r.setPeriodoClave(periodoClave);
        r.setEstado("PENDIENTE");
        r.setComentario(comentario);
        r.setTotalDebitos(debe);
        r.setTotalHaber(haber);
        r.setSaldoFinal(debe.subtract(haber));
        return reporteDAO.save(r);
    }

    /* ========= EDITAR ========= */
    @Transactional
    public Reporte editarSiNoAprobado(Long id, String parametrosJson, String comentario) {
        Reporte r = reporteDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el reporte"));
        if ("APROBADO".equals(r.getEstado()))
            throw new IllegalStateException("No se puede editar un APROBADO");

        if (comentario!=null) r.setComentario(comentario);

        // Recalcular totales del mismo período
        Periodo.PeriodoCalc p = claveARango(r.getPeriodicidad(), r.getPeriodoClave());
        Map<String,Object> resumen = balanzaService.consultar(p.inicio(), p.fin());
        BigDecimal debe  = (BigDecimal) resumen.getOrDefault("totalDebitos",  BigDecimal.ZERO);
        BigDecimal haber = (BigDecimal) resumen.getOrDefault("totalCreditos", BigDecimal.ZERO);
        r.setTotalDebitos(debe);
        r.setTotalHaber(haber);
        r.setSaldoFinal(debe.subtract(haber));

        if ("RECHAZADO".equals(r.getEstado())) {
            r.setEstado("PENDIENTE");
            r.setComentarioRevision(null);
        }
        return reporteDAO.save(r);
    }

    /* ========= WORKFLOW ========= */
    @Transactional
    public Reporte aprobar(Long id) {
        Reporte r = reporteDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el reporte"));
        if (!"PENDIENTE".equals(r.getEstado()))
            throw new IllegalStateException("Solo PENDIENTE puede aprobarse");

        r.setArchivoUrl(generarPdf(r));
        r.setEstado("APROBADO");
        return reporteDAO.save(r);
    }

    @Transactional
    public Reporte rechazar(Long id, String motivo) {
        if (motivo==null || motivo.isBlank())
            throw new IllegalArgumentException("Motivo requerido");
        Reporte r = reporteDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el reporte"));
        if (!"PENDIENTE".equals(r.getEstado()))
            throw new IllegalStateException("Solo PENDIENTE puede rechazarse");

        r.setEstado("RECHAZADO");
        r.setComentarioRevision(motivo);
        return reporteDAO.save(r);
    }

    public List<Reporte> listarPendientes(){ return reporteDAO.findByEstadoOrderByCreatedAtDesc("PENDIENTE"); }
    public List<Reporte> listarAprobados(){ return reporteDAO.findByEstadoOrderByCreatedAtDesc("APROBADO"); }
    public List<Reporte> listarRechazados(){ return reporteDAO.findByEstadoOrderByCreatedAtDesc("RECHAZADO"); }

    /* ========= PDF ========= */
    private String generarPdf(Reporte r) {
        Path dir = Paths.get("uploads", "reportes");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}

        String nombre = String.format("%s_%s_%s_%d.pdf",
                String.valueOf(r.getTipoReporte()),
                String.valueOf(r.getPeriodicidad()),
                String.valueOf(r.getPeriodoClave()),
                r.getIdReporte());

        Path file = dir.resolve(nombre);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 56f;
                float y = page.getMediaBox().getHeight() - margin;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(margin, y);
                cs.showText("SUITE BALANCE | ISB - REPORTE");
                cs.endText();

                y -= 28;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(margin, y);

                String comentario = (r.getComentario()==null || r.getComentario().isBlank())
                        ? "(sin comentario)" : r.getComentario();

                String[] lines = new String[]{
                        "Tipo         : " + String.valueOf(r.getTipoReporte()),
                        "Periodicidad : " + String.valueOf(r.getPeriodicidad()),
                        "Periodo      : " + String.valueOf(r.getPeriodoClave()),
                        "Estado       : " + String.valueOf(r.getEstado()),
                        "Comentario   : " + comentario,
                        "",
                        "--- RESUMEN CONTABLE ---",
                        "Total Débitos: " + nv(r.getTotalDebitos()),
                        "Total Creditos  : " + nv(r.getTotalHaber()),
                        "Saldo Final  : " + nv(r.getSaldoFinal()),
                        "",
                        "Generado     : " + java.time.LocalDateTime.now()
                };
                for (String ln : lines) {
                    cs.showText(ln);
                    cs.newLineAtOffset(0, -16);
                }
                cs.endText();
            }
            doc.save(file.toFile());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el PDF", e);
        }

        return "/uploads/reportes/" + file.getFileName().toString();
    }

    private String nv(BigDecimal x) {
        return x == null ? "0.00" : x.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /* Reconstruir rango a partir de la clave (para recalcular) */
    private Periodo.PeriodoCalc claveARango(String periodicidad, String clave) {
        return switch (periodicidad.toUpperCase()) {
            case "MENSUAL" -> {
                int y = Integer.parseInt(clave.substring(0,4));
                int m = Integer.parseInt(clave.substring(5,7));
                var ini = java.time.LocalDate.of(y, m, 1);
                var fin = ini.withDayOfMonth(ini.lengthOfMonth());
                yield new Periodo.PeriodoCalc(clave, ini, fin);
            }
            case "BIMESTRAL" -> {
                int y = Integer.parseInt(clave.substring(0,4));
                int bi = Integer.parseInt(clave.substring(7));
                int startMonth = (bi - 1) * 2 + 1;
                var ini = java.time.LocalDate.of(y, startMonth, 1);
                var fin = ini.plusMonths(2).minusDays(1);
                yield new Periodo.PeriodoCalc(clave, ini, fin);
            }
            case "TRIMESTRAL" -> {
                int y = Integer.parseInt(clave.substring(0,4));
                int q = Integer.parseInt(clave.substring(7));
                int startMonth = (q - 1) * 3 + 1;
                var ini = java.time.LocalDate.of(y, startMonth, 1);
                var fin = ini.plusMonths(3).minusDays(1);
                yield new Periodo.PeriodoCalc(clave, ini, fin);
            }
            case "ANUAL" -> {
                int y = Integer.parseInt(clave);
                var ini = java.time.LocalDate.of(y, 1, 1);
                var fin = java.time.LocalDate.of(y, 12, 31);
                yield new Periodo.PeriodoCalc(clave, ini, fin);
            }
            default -> throw new IllegalArgumentException("Periodicidad inválida: " + periodicidad);
        };
    }
}
