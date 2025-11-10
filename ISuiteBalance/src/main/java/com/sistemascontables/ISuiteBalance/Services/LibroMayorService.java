package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import com.sistemascontables.ISuiteBalance.Repositorios.CuentaContableDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.LibroMayorView;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class LibroMayorService {

    public static class FilaMayor {
        public LocalDate fecha;
        public Long idPartida;
        public String concepto;
        public BigDecimal debe;
        public BigDecimal haber;
        public BigDecimal saldo;  // acumulado con la naturaleza correcta
        public Long docId;
        public String docNombre;
    }

    public static class GrupoMayor {
        public Long idCuenta;
        public String codigo;
        public String nombrecuenta;
        public String tipoCuenta;               // ACTIVO/PASIVO/... (para saber naturaleza)
        public List<FilaMayor> filas = new ArrayList<>();
        public BigDecimal totalDebe  = BigDecimal.ZERO;
        public BigDecimal totalHaber = BigDecimal.ZERO;
        public BigDecimal saldoInicial = BigDecimal.ZERO;   // en signo “natural” de la cuenta
        public BigDecimal saldoFinal   = BigDecimal.ZERO;   // en signo “natural”
    }

    private final DetallePartidaDAO detalleRepo;
    private final CuentaContableDAO cuentaRepo;
    private final BalanzaComprobacionService balanzaService;

    public LibroMayorService(DetallePartidaDAO detalleRepo, CuentaContableDAO cuentaRepo, BalanzaComprobacionService balanzaService) {
        this.detalleRepo = detalleRepo;
        this.cuentaRepo = cuentaRepo;
        this.balanzaService = balanzaService;
    }

    // Naturaleza: true = acreedora (Pasivo/Patrimonio/Ingreso), false = deudora (Activo/Gasto/Costo)
    private static boolean esAcreedora(String tipo) {
        if (tipo == null) return false;
        String t = tipo.trim().toUpperCase();
        return t.equals("PASIVO") || t.equals("PATRIMONIO") || t.equals("CAPITAL") || t.equals("INGRESO");
    }

    private static BigDecimal nvl(BigDecimal x){ return x == null ? BigDecimal.ZERO : x; }

    public Map<String,Object> consultar(Long idCuenta, LocalDate desde, LocalDate hasta) {
        // 0) Catálogo para conocer tipo/naturaleza y saldos iniciales
        List<CuentaContable> cuentas = cuentaRepo.findAllByOrderByCodigoAsc();
        Map<Long, CuentaContable> porId = new HashMap<>();
        for (var c : cuentas) porId.put(c.getIdCuenta(), c);

        // 1) Movimientos del rango
        var movs = detalleRepo.mayorMovimientos(idCuenta, desde, hasta);

        // 2) Saldos iniciales por cuenta provenientes de movimientos ANTERIORES al "desde"
        //    (en convención Debe-Haber: saldo = SUM(debe - haber))
        Map<Long, BigDecimal> siDebHab = new HashMap<>();
        if (desde != null) {
            detalleRepo.saldosInicialesPorCuenta(idCuenta, desde)
                    .forEach(r -> siDebHab.put(r.getIdCuenta(), nvl(r.getSaldo())));
        }

        // 2.1) Integrar el saldo_inicial del catálogo (tbl_cuentacontable)
        //      a la misma convención Debe-Haber:
        //      - Cuentas de naturaleza DEUDORA (Activo/Gasto/Costo):  +saldo_inicial
        //      - Cuentas de naturaleza ACREEDORA (Pasivo/Patrimonio/Ingreso): -saldo_inicial
        //      Solo se suma si la fecha_saldo_inicial es nula o es <= "desde" (o si "desde" es nulo).
        for (CuentaContable c : cuentas) {
            if (idCuenta != null && !Objects.equals(idCuenta, c.getIdCuenta())) continue;

            BigDecimal cat = nvl(c.getSaldoInicial());
            if (cat.signum() == 0) continue;

            boolean incluirCatalogo;
            if (desde == null) {
                incluirCatalogo = true; // sin filtro de fecha, incluye el saldo inicial del catálogo
            } else {
                LocalDate fsi = c.getFechaSaldoInicial();
                incluirCatalogo = (fsi == null) || !fsi.isAfter(desde); // fsi <= desde
            }

            if (incluirCatalogo) {
                boolean acre = esAcreedora(c.getTipocuenta());
                BigDecimal catDebeHaber = acre ? cat.negate() : cat; // convertir a convención Debe-Haber
                siDebHab.merge(c.getIdCuenta(), catDebeHaber, BigDecimal::add);
            }
        }

        // 3) Agrupar por cuenta aplicando naturaleza y partiendo del saldo inicial (natural)
        Map<Long, GrupoMayor> grupos = new LinkedHashMap<>();

        for (LibroMayorView m : movs) {
            var g = grupos.computeIfAbsent(m.getIdCuenta(), k -> {
                var ng = new GrupoMayor();
                ng.idCuenta = m.getIdCuenta();
                ng.codigo = m.getCodigo();
                ng.nombrecuenta = m.getNombrecuenta();

                CuentaContable cc = porId.get(m.getIdCuenta());
                ng.tipoCuenta = (cc == null ? null : cc.getTipocuenta());

                // saldo inicial en Debe-Haber (de movimientos + catálogo)
                BigDecimal baseDebeHaber = siDebHab.getOrDefault(m.getIdCuenta(), BigDecimal.ZERO);

                // convertir ese saldo Debe-Haber a signo “natural” de la cuenta
                // natural = (acre) (H-D) ; pero tenemos (D-H) → natural = -(D-H) para acreedoras
                BigDecimal baseNatural = esAcreedora(ng.tipoCuenta) ? baseDebeHaber.negate() : baseDebeHaber;

                ng.saldoInicial = baseNatural;
                return ng;
            });

            BigDecimal d = nvl(m.getDebe());
            BigDecimal h = nvl(m.getHaber());

            g.totalDebe  = g.totalDebe.add(d);
            g.totalHaber = g.totalHaber.add(h);

            // saldo corrido según naturaleza
            BigDecimal saldoPrev = g.filas.isEmpty() ? g.saldoInicial : g.filas.get(g.filas.size()-1).saldo;
            BigDecimal delta = esAcreedora(g.tipoCuenta) ? h.subtract(d) : d.subtract(h);
            BigDecimal saldoNuevo = saldoPrev.add(delta);

            var fila = new FilaMayor();
            fila.fecha = m.getFecha();
            fila.idPartida = m.getIdPartida();
            fila.concepto = m.getDescripcion();
            fila.debe = d;
            fila.haber = h;
            fila.docId = m.getDocId();
            fila.docNombre = m.getDocNombre();
            fila.saldo = saldoNuevo;

            g.filas.add(fila);
            g.saldoFinal = saldoNuevo;
        }

        Map<String,Object> out = new HashMap<>();
        out.put("grupos", new ArrayList<>(grupos.values()));
        out.put("cuentas", cuentas);         // para combos en UI si los usas
        out.put("idCuenta", idCuenta);
        out.put("desdeStr", desde == null ? null : desde.toString());
        out.put("hastaStr", hasta == null ? null : hasta.toString());
        return out;
    }

    public Map<String,Object> consultarTodas(LocalDate inicio, LocalDate fin) {
        // Reutilizamos tu cálculo de balanza (ya devuelve por cuenta + totales)
        // y simplemente renombramos claves para la vista.
        Map<String,Object> balanza = balanzaService.consultar(inicio, fin);

        // La balanza suele traer:
        // - "filas": List<FilaCuenta>  (codigo, nombre, saldoInicial, debitos, creditos, saldoFinalDeudor, saldoFinalAcreedor)
        // - "totalDebitos" / "totalCreditos" / etc.
        // Ajusta si tus claves difieren.
        return Map.of(
                "filas", balanza.get("filas"),
                "totalDebitos", balanza.get("totalDebitos"),
                "totalCreditos", balanza.get("totalCreditos"),
                "totalFinalDeudor", balanza.get("totalFinalDeudor"),
                "totalFinalAcreedor", balanza.get("totalFinalAcreedor"),
                "titulo", "Libro Mayor (resumen) — " + inicio + " a " + fin
        );
    }
}
