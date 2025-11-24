package com.sistemascontables.ISuiteBalance.Models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class Periodo {

    private Periodo(){}

    public record Rango(LocalDate inicio, LocalDate fin){}
    public record PeriodoCalc(String clave, LocalDate inicio, LocalDate fin){}

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    /* ====== Visualizar: período EN CURSO hasta hoy ====== */
    public static Rango enCursoHoy(String periodicidad) {
        LocalDate hoy = LocalDate.now();
        return switch (periodicidad.toUpperCase()) {
            case "MENSUAL"    -> new Rango(hoy.withDayOfMonth(1), hoy);
            case "BIMESTRAL"  -> {
                int m = hoy.getMonthValue();
                int start = ((m - 1) / 2) * 2 + 1;          // 1,3,5,7,9,11
                LocalDate ini = LocalDate.of(hoy.getYear(), start, 1);
                yield new Rango(ini, hoy);
            }
            case "TRIMESTRAL" -> {
                int q = ((hoy.getMonthValue() - 1) / 3) + 1; // 1..4
                LocalDate ini = LocalDate.of(hoy.getYear(), (q - 1) * 3 + 1, 1);
                yield new Rango(ini, hoy);
            }
            case "ANUAL"      -> new Rango(LocalDate.of(hoy.getYear(), 1, 1), hoy);
            default -> throw new IllegalArgumentException("Periodicidad inválida: " + periodicidad);
        };
    }

    /* ====== Crear: ÚLTIMO período CERRADO ====== */
    public static PeriodoCalc ultimoCerrado(String periodicidad) {
        LocalDate hoy = LocalDate.now();

        return switch (periodicidad.toUpperCase()) {
            case "MENSUAL" -> {
                LocalDate mes = hoy.minusMonths(1);
                LocalDate ini = mes.withDayOfMonth(1);
                LocalDate fin = mes.withDayOfMonth(mes.lengthOfMonth());
                yield new PeriodoCalc(ini.format(YM), ini, fin);            // 2025-10
            }
            case "BIMESTRAL" -> {
                int m = hoy.getMonthValue();
                int startCurso = ((m - 1) / 2) * 2 + 1; // 1,3,5,7,9,11
                LocalDate iniCurso = LocalDate.of(hoy.getYear(), startCurso, 1);
                LocalDate finCurso = iniCurso.plusMonths(2).minusDays(1);
                LocalDate ini = hoy.isBefore(finCurso.plusDays(1)) ? iniCurso.minusMonths(2) : iniCurso;
                LocalDate fin = ini.plusMonths(2).minusDays(1);
                int bi = ((ini.getMonthValue() - 1) / 2) + 1;               // 1..6
                String clave = ini.getYear() + "-B" + bi;                   // 2025-B5
                yield new PeriodoCalc(clave, ini, fin);
            }
            case "TRIMESTRAL" -> {
                int qCurso = ((hoy.getMonthValue() - 1) / 3) + 1;           // 1..4
                LocalDate iniCurso = LocalDate.of(hoy.getYear(), (qCurso - 1) * 3 + 1, 1);
                LocalDate finCurso = iniCurso.plusMonths(3).minusDays(1);
                LocalDate ini = hoy.isBefore(finCurso.plusDays(1)) ? iniCurso.minusMonths(3) : iniCurso;
                LocalDate fin = ini.plusMonths(3).minusDays(1);
                int q = ((ini.getMonthValue() - 1) / 3) + 1;                // 1..4
                String clave = ini.getYear() + "-Q" + q;                    // 2025-Q3
                yield new PeriodoCalc(clave, ini, fin);
            }
            case "ANUAL" -> {
                int year = hoy.getYear() - 1;
                LocalDate ini = LocalDate.of(year, 1, 1);
                LocalDate fin = LocalDate.of(year, 12, 31);
                yield new PeriodoCalc(String.valueOf(year), ini, fin);      // 2024
            }
            default -> throw new IllegalArgumentException("Periodicidad inválida: " + periodicidad);
        };
    }
}
