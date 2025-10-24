package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import com.sistemascontables.ISuiteBalance.Repositorios.CuentaContableDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaDAO;
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
        public BigDecimal saldo;  // acumulado dentro de la cuenta
        public Long docId;
        public String docNombre;
    }

    public static class GrupoMayor {
        public Long idCuenta;
        public String codigo;
        public String nombrecuenta;
        public List<FilaMayor> filas = new ArrayList<>();
        public BigDecimal totalDebe  = BigDecimal.ZERO;
        public BigDecimal totalHaber = BigDecimal.ZERO;
        public BigDecimal saldoFinal = BigDecimal.ZERO;
    }

    private final DetallePartidaDAO detalleRepo;
    private final CuentaContableDAO cuentaRepo; // si ya lo tienes

    public LibroMayorService(DetallePartidaDAO detalleRepo, CuentaContableDAO cuentaRepo) {
        this.detalleRepo = detalleRepo;
        this.cuentaRepo = cuentaRepo;
    }

    public Map<String,Object> consultar(Long idCuenta, LocalDate desde, LocalDate hasta) {
        var movs = detalleRepo.mayorMovimientos(idCuenta, desde, hasta);

        // Agrupar por cuenta:
        Map<Long, GrupoMayor> grupos = new LinkedHashMap<>();
        for (var m : movs) {
            var g = grupos.computeIfAbsent(m.getIdCuenta(), k -> {
                var ng = new GrupoMayor();
                ng.idCuenta = m.getIdCuenta();
                ng.codigo = m.getCodigo();
                ng.nombrecuenta = m.getNombrecuenta();
                return ng;
            });

            BigDecimal d = m.getDebe()  == null ? BigDecimal.ZERO : m.getDebe();
            BigDecimal h = m.getHaber() == null ? BigDecimal.ZERO : m.getHaber();
            g.totalDebe  = g.totalDebe.add(d);
            g.totalHaber = g.totalHaber.add(h);
            g.saldoFinal = g.saldoFinal.add(d).subtract(h); // Debe(+) Haber(-)

            var fila = new FilaMayor();
            fila.fecha = m.getFecha();
            fila.idPartida = m.getIdPartida();
            fila.concepto = m.getDescripcion();
            fila.debe = d;
            fila.haber = h;
            fila.docId = m.getDocId();
            fila.docNombre = m.getDocNombre();
            // saldo corrido = saldoFinal acumulado hasta esta fila
            fila.saldo = (g.filas.isEmpty() ? BigDecimal.ZERO : g.filas.get(g.filas.size()-1).saldo)
                    .add(d).subtract(h);

            g.filas.add(fila);
        }

        // Catálogo para el filtro de cuentas (combo):
        List<CuentaContable> cuentas = cuentaRepo.findAllByOrderByCodigoAsc();

        Map<String,Object> out = new HashMap<>();
        out.put("grupos", new ArrayList<>(grupos.values())); // List<GrupoMayor> en orden de inserción
        out.put("cuentas", cuentas);
        out.put("idCuenta", idCuenta);
        out.put("desdeStr", desde == null ? null : desde.toString());
        out.put("hastaStr", hasta == null ? null : hasta.toString());
        return out;
    }
}

