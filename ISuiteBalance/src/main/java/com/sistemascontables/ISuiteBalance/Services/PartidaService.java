package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.*;
import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.PartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class PartidaService {
    private final PartidaDAO partidaRepo;
    private final DetallePartidaDAO detalleRepo;
    private final DocumentoService documentoService;
    private final DocumentoFuenteDAO documentoRepo;


    public PartidaService(PartidaDAO p, DetallePartidaDAO d, DocumentoService doc, DocumentoFuenteDAO documentoRepo) {
        this.partidaRepo = p; this.detalleRepo = d; this.documentoService = doc;
        this.documentoRepo = documentoRepo;
    }


    // Listado paginado para Gestión de Partidas
    public Page<PartidaResumen> listarResumen(int page, int size) {
        return partidaRepo.listarResumen(PageRequest.of(page, size));
    }

    // Detalle con nombre de cuenta (para la vista)
    public List<DetallePartidaView> obtenerLineasConNombre(Integer idPartida) {
        return detalleRepo.lineasConNombre(idPartida.longValue());
    }

    // Alias para que compile tu DashController actual
    public List<DetallePartidaView> obtenerLineas(Integer idPartida) {
        return obtenerLineasConNombre(idPartida);
    }

    // Eliminar partida + sus detalles
    @Transactional
    public void eliminarPartida(Integer idPartida) {
        Long id = idPartida.longValue();
        detalleRepo.deleteByIdPartida(id);
        partidaRepo.deleteById(id);
    }

    @Transactional
    public Long guardarPartida(PartidaRequest req, Long idDocumento) {
        // 1) Cabecera
        Partida p = new Partida();
        p.setFecha(req.getFecha());                         // puedes tomar la del primer detalle si no envías una de cabecera
        p.setConcepto(req.getConcepto());
        p.setIdUsuario(req.getIdUsuario());                 // si ya tienes usuario logueado puedes inyectarlo aquí
        p = partidaRepo.save(p);

        // 2) Detalles
        for (LineaDetalle ld : req.getDetalles()) {
            DetallePartida d = new DetallePartida();
            d.setIdPartida(p.getId());
            d.setIdCuenta(ld.getIdCuenta());
            d.setMontoDebe(ld.getDebe());
            d.setMontoHaber(ld.getHaber());
            detalleRepo.save(d);
        }

        // 3) Vincular documento (obligatorio 1 x partida)
        documentoService.vincularADPartida(idDocumento, p.getId());

        return p.getId();
    }

    // ViewModel para la pantalla de edición
    public static record PartidaEditView(
            Partida partida,
            List<DetallePartidaView> lineas,
            Optional<DocumentoFuente> documento
    ) {}

    // Cargar cabecera + líneas + documento para la vista de edición
    public PartidaEditView cargarParaEditar(Integer idPartida) {
        Partida p = partidaRepo.findById(idPartida.longValue())
                .orElseThrow(() -> new IllegalArgumentException("La partida no existe"));
        List<DetallePartidaView> lineas = detalleRepo.lineasConNombre(p.getId());
        Optional<DocumentoFuente> doc = documentoRepo.findByIdPartida(p.getId());
        return new PartidaEditView(p, lineas, doc);
    }

    @Transactional
    public void actualizarPartida(Integer idPartida,
                                  LocalDate fecha,
                                  String concepto,
                                  List<Long> idsCuenta,
                                  List<BigDecimal> debes,
                                  List<BigDecimal> haberes,
                                  MultipartFile nuevoPdf,
                                  Integer idClasificacionDocumento) throws Exception {

        // 1) Cargar partida existente
        Partida p = partidaRepo.findById(idPartida.longValue())
                .orElseThrow(() -> new IllegalArgumentException("La partida no existe"));

        // 2) Actualizar cabecera
        p.setFecha(fecha);
        p.setConcepto(concepto);
        partidaRepo.save(p);

        // 3) Documento: si viene nuevo PDF, lo reemplazamos
        if (nuevoPdf != null && !nuevoPdf.isEmpty()) {
            documentoService.reemplazarPDFParaPartida(p.getId(), nuevoPdf, idClasificacionDocumento);
        }

        // 4) Validación: la partida debe tener documento vinculado
        if (documentoRepo.findByIdPartida(p.getId()).isEmpty()) {
            throw new IllegalStateException("La partida debe tener un documento adjuntado.");
        }

        // 5) Reemplazar líneas: estrategia simple (borrar todo y reinsertar)
        detalleRepo.deleteByIdPartida(p.getId());

        int n = idsCuenta == null ? 0 : idsCuenta.size();
        for (int i = 0; i < n; i++) {
            Long idCuenta = idsCuenta.get(i);

            BigDecimal debe = BigDecimal.ZERO;
            if (debes != null && i < debes.size() && debes.get(i) != null) {
                debe = debes.get(i);
            }

            BigDecimal haber = BigDecimal.ZERO;
            if (haberes != null && i < haberes.size() && haberes.get(i) != null) {
                haber = haberes.get(i);
            }

            DetallePartida d = new DetallePartida();
            d.setIdPartida(p.getId());
            d.setIdCuenta(idCuenta);
            d.setMontoDebe(debe);
            d.setMontoHaber(haber);
            detalleRepo.save(d);
        }

        // (Opcional) Verificación de cuadre:
        // BigDecimal sumDebe = detalleRepo.sumDebePorPartida(p.getId());
        // BigDecimal sumHaber = detalleRepo.sumHaberPorPartida(p.getId());
        // if (sumDebe.compareTo(sumHaber) != 0) throw new IllegalStateException("La partida no cuadra.");
    }

}
