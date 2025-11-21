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

    /* ========= CONTADORES PARA EL DASHBOARD ========= */

    public long contarPorEstado(String estado) {
        return reporteDAO.countByEstado(estado);
    }

    public long contarPendientes() {
        return contarPorEstado("PENDIENTE");
    }

    public long contarAprobados() {
        return contarPorEstado("APROBADO");
    }

    public long contarRechazados() {
        return contarPorEstado("RECHAZADO");
    }

    // Si más adelante tienes un estado extra tipo "REVISION", aquí lo ajustas.
    public long contarEnRevision() {
        // Por ahora consideramos "en revisión" = PENDIENTE
        return contarPendientes();
    }

    /* ========= BÚSQUEDAS ========= */

    @Transactional(readOnly = true)
    public Reporte buscarPorId(Long id){
        return reporteDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el reporte con id=" + id));
    }

    /* ========= CREAR (último período cerrado) ========= */
    @Transactional
    public Reporte crearPendiente(String tipo, String periodicidad,
                                  String periodoClave,
                                  LocalDate inicio, LocalDate fin,
                                  String parametrosJson,
                                  String comentario) {

        // Regla de NO duplicados (tipo + periodicidad + periodoClave)
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
    public Reporte editarSiNoAprobado(Long id, String comentario) {
        Reporte r = reporteDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el reporte"));

        if ("APROBADO".equals(r.getEstado()))
            throw new IllegalStateException("No se puede editar un APROBADO");

        // Solo permitimos cambiar comentario y recalcular montos del mismo período
        if (comentario != null) {
            r.setComentario(comentario);
        }

        // Recalcular totales del mismo período usando periodicidad + periodoClave
        Periodo.PeriodoCalc p = claveARango(r.getPeriodicidad(), r.getPeriodoClave());
        Map<String,Object> resumen = balanzaService.consultar(p.inicio(), p.fin());
        BigDecimal debe  = (BigDecimal) resumen.getOrDefault("totalDebitos",  BigDecimal.ZERO);
        BigDecimal haber = (BigDecimal) resumen.getOrDefault("totalCreditos", BigDecimal.ZERO);
        r.setTotalDebitos(debe);
        r.setTotalHaber(haber);
        r.setSaldoFinal(debe.subtract(haber));

        // Si estaba RECHAZADO, vuelve a PENDIENTE y se limpia el comentario de revisión
        if ("RECHAZADO".equals(r.getEstado())) {
            r.setEstado("PENDIENTE");
            r.setComentarioRevision(null);
        }
        return reporteDAO.save(r);
    }

    @Transactional
    public Reporte aprobar(Long id) {
        Reporte r = reporteDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el reporte"));
        if (!"PENDIENTE".equals(r.getEstado()))
            throw new IllegalStateException("Solo PENDIENTE puede aprobarse");

        r.setEstado("APROBADO");

        r.setArchivoUrl(generarPdf(r));

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

    /* ========= GENERACIÓN DE PDF ========= */

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

            PDRectangle mediaBox = page.getMediaBox();
            float margin = 56f;
            float width  = mediaBox.getWidth() - 2 * margin;
            float y      = mediaBox.getHeight() - margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                /* ==== TÍTULO CENTRADO ==== */
                String titulo    = "SUITE BALANCE | ISB";
                String subtitulo = "REPORTE DE " + r.getTipoReporte();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(titulo) / 1000 * 18;
                float titleX     = margin + (width - titleWidth) / 2;

                cs.beginText();
                cs.newLineAtOffset(titleX, y);
                cs.showText(titulo);
                cs.endText();

                y -= 24;

                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                float subWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(subtitulo) / 1000 * 14;
                float subX     = margin + (width - subWidth) / 2;

                cs.beginText();
                cs.newLineAtOffset(subX, y);
                cs.showText(subtitulo);
                cs.endText();

                y -= 32;

                /* ==== RECUADRO DATOS GENERALES ==== */
                float datosBoxHeight = 110f;
                float datosTop       = y;
                float datosBottom    = datosTop - datosBoxHeight;

                cs.setLineWidth(0.5f);
                cs.addRect(margin, datosBottom, width, datosBoxHeight);
                cs.stroke();

                // Título del recuadro
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(margin + 8, datosTop - 16);
                cs.showText("Datos generales");
                cs.endText();

                // Texto interno
                String comentario = (r.getComentario() == null || r.getComentario().isBlank())
                        ? "(sin comentario)" : r.getComentario();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(margin + 14, datosTop - 32);
                cs.showText("Tipo         : " + r.getTipoReporte());
                cs.newLineAtOffset(0, -14);
                cs.showText("Periodicidad : " + r.getPeriodicidad());
                cs.newLineAtOffset(0, -14);
                cs.showText("Periodo      : " + r.getPeriodoClave());
                cs.newLineAtOffset(0, -14);
                cs.showText("Estado       : " + r.getEstado());
                cs.newLineAtOffset(0, -14);
                cs.showText("Comentario   : " + comentario);
                cs.endText();

                // Ahora dejamos y justo por debajo del recuadro + margen
                y = datosBottom - 24;

                /* ==== TABLA RESUMEN CONTABLE ==== */
                float rowHeight   = 18f;
                String[][] filas  = new String[][]{
                        {"Total Débitos",  nv(r.getTotalDebitos())},
                        {"Total Créditos", nv(r.getTotalHaber())},
                        {"Saldo Final",    nv(r.getSaldoFinal())}
                };

                // Altura: 1 fila para el título + 1 fila para encabezados + n filas de datos
                float tableHeight = rowHeight * (filas.length + 2);
                float col1Width   = width * 0.55f;
                float col2Width   = width - col1Width;

                float tableTop    = y;
                float tableBottom = tableTop - tableHeight;

                // Marco exterior
                cs.addRect(margin, tableBottom, width, tableHeight);
                cs.stroke();

                // Línea vertical entre columnas
                cs.moveTo(margin + col1Width, tableBottom);
                cs.lineTo(margin + col1Width, tableBottom + tableHeight);
                cs.stroke();

                // Línea horizontal que separa título/encabezados
                float headerLineY = tableBottom + tableHeight - rowHeight;
                cs.moveTo(margin, headerLineY);
                cs.lineTo(margin + width, headerLineY);
                cs.stroke();

                /* --- Título de la tabla --- */
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(margin + 8, tableBottom + tableHeight - rowHeight + 4);
                cs.showText("RESUMEN CONTABLE");
                cs.endText();

                /* --- Encabezados de columnas --- */
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                cs.newLineAtOffset(margin + 8, headerLineY - 18);
                cs.showText("Concepto");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                cs.newLineAtOffset(margin + col1Width + 8, headerLineY - 18);
                cs.showText("Valor");
                cs.endText();

                /* --- Filas de datos --- */
                float currentY = headerLineY - rowHeight - 10; // primera fila de datos

                cs.setFont(PDType1Font.HELVETICA, 11);
                for (String[] fila : filas) {
                    cs.beginText();
                    cs.newLineAtOffset(margin + 8, currentY - 4);
                    cs.showText(fila[0]);
                    cs.endText();

                    cs.beginText();
                    cs.newLineAtOffset(margin + col1Width + 8, currentY - 4);
                    cs.showText(fila[1]);
                    cs.endText();

                    currentY -= rowHeight;
                }

                /* ==== FECHA ==== */
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.newLineAtOffset(margin, tableBottom - 30);
                cs.showText("Generado: " + java.time.LocalDateTime.now());
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

    /* Reconstruir rango a partir de la clave*/
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
