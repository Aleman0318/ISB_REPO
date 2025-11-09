package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import com.sistemascontables.ISuiteBalance.Repositorios.CuentaContableDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class SaldosService {

    private final CuentaContableDAO cuentaRepo;
    private final DetallePartidaDAO detalleRepo;

    public SaldosService(CuentaContableDAO cuentaRepo, DetallePartidaDAO detalleRepo) {
        this.cuentaRepo = cuentaRepo;
        this.detalleRepo = detalleRepo;
    }

    /* =========================
     * 1) Lecturas / Cálculos
     * ========================= */

    /** Devuelve { saldoInicial, debitos, creditos, saldoFinal } para una cuenta en el rango. */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> resumenSaldo(Long idCuenta, LocalDate desde, LocalDate hasta) {
        CuentaContable cta = cuentaRepo.findById(idCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + idCuenta));

        BigDecimal si  = nvl(cta.getSaldoInicial());
        BigDecimal deb = nvl(detalleRepo.sumDebitos(idCuenta, desde, hasta));
        BigDecimal cre = nvl(detalleRepo.sumCreditos(idCuenta, desde, hasta));

        BigDecimal saldoFinal = aplicarNaturaleza(cta, si, deb, cre);

        return Map.of(
                "saldoInicial", si,
                "debitos", deb,
                "creditos", cre,
                "saldoFinal", saldoFinal
        );
    }

    /** Calcula saldo final (SI + movimientos) aplicando naturaleza. */
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoFinal(Long idCuenta, LocalDate desde, LocalDate hasta) {
        CuentaContable cta = cuentaRepo.findById(idCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + idCuenta));

        BigDecimal si  = nvl(cta.getSaldoInicial());
        BigDecimal deb = nvl(detalleRepo.sumDebitos(idCuenta, desde, hasta));
        BigDecimal cre = nvl(detalleRepo.sumCreditos(idCuenta, desde, hasta));

        return aplicarNaturaleza(cta, si, deb, cre);
    }

    /* Regla:
       - Activos/Gastos/Costo (naturaleza DEUDORA):  saldo = SI + Debe - Haber
       - Pasivos/Patrimonio/Ingresos (ACREEDORA):     saldo = SI - Debe + Haber
       Usa el campo tipocuenta de tu modelo (ACTIVO, PASIVO, PATRIMONIO, INGRESO, GASTO, COSTO). */
    private BigDecimal aplicarNaturaleza(CuentaContable cta, BigDecimal si, BigDecimal deb, BigDecimal cre) {
        String tipo = (cta.getTipocuenta() == null) ? "" : cta.getTipocuenta().trim().toUpperCase();
        boolean deudora = tipo.equals("ACTIVO") || tipo.equals("GASTO") || tipo.equals("COSTO");

        return deudora
                ? si.add(deb).subtract(cre)
                : si.subtract(deb).add(cre);
    }

    private static BigDecimal nvl(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }

    /* =========================
     * 2) Escrituras / Altas
     * ========================= */

    /** Actualiza saldo inicial (y opcional fecha) de una cuenta existente por ID. */
    @Transactional
    public CuentaContable actualizarSaldoInicial(Long idCuenta, BigDecimal saldoInicial, LocalDate fechaSaldoInicial) {
        if (saldoInicial == null) saldoInicial = BigDecimal.ZERO;

        CuentaContable cta = cuentaRepo.findById(idCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + idCuenta));

        cta.setSaldoInicial(saldoInicial);
        if (fechaSaldoInicial != null) {
            cta.setFechaSaldoInicial(fechaSaldoInicial);
        }
        return cuentaRepo.save(cta);
    }

    /**
     * Crea o actualiza una cuenta por su código:
     * - Si NO existe: crea la cuenta con [codigo, nombre, tipo, saldoInicial, fecha]
     * - Si SÍ existe: solo actualiza saldoInicial (y opcionalmente fecha, nombre y tipo si vienen)
     */
    @Transactional
    public CuentaContable asegurarCuentaYSaldo(String codigo,
                                               String nombrecuenta,
                                               String tipocuenta,
                                               BigDecimal saldoInicial,
                                               LocalDate fechaSaldoInicial) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la cuenta es obligatorio.");
        }
        if (saldoInicial == null) saldoInicial = BigDecimal.ZERO;

        Optional<CuentaContable> ya = cuentaRepo.findByCodigo(codigo);
        if (ya.isPresent()) {
            // ACTUALIZAR
            CuentaContable cta = ya.get();
            cta.setSaldoInicial(saldoInicial);
            if (fechaSaldoInicial != null) cta.setFechaSaldoInicial(fechaSaldoInicial);
            if (nombrecuenta != null && !nombrecuenta.isBlank()) cta.setNombrecuenta(nombrecuenta);
            if (tipocuenta   != null && !tipocuenta.isBlank())   cta.setTipocuenta(tipocuenta);
            return cuentaRepo.save(cta);
        } else {
            // CREAR
            if (nombrecuenta == null || nombrecuenta.isBlank()) {
                throw new IllegalArgumentException("Para crear, 'nombrecuenta' es obligatorio.");
            }
            if (tipocuenta == null || tipocuenta.isBlank()) {
                throw new IllegalArgumentException("Para crear, 'tipocuenta' es obligatorio (ACTIVO, PASIVO, PATRIMONIO, INGRESO, GASTO, COSTO).");
            }
            CuentaContable cta = new CuentaContable();
            cta.setCodigo(codigo);
            cta.setNombrecuenta(nombrecuenta);
            cta.setTipocuenta(tipocuenta);
            cta.setSaldoInicial(saldoInicial);
            cta.setFechaSaldoInicial(fechaSaldoInicial);
            return cuentaRepo.save(cta);
        }
    }
}
