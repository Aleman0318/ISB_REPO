package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.DetallePartida;
import com.sistemascontables.ISuiteBalance.Models.LineaDetalle;
import com.sistemascontables.ISuiteBalance.Models.Partida;
import com.sistemascontables.ISuiteBalance.Models.PartidaRequest;
import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.PartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class PartidaService {
    private final PartidaDAO partidaRepo;
    private final DetallePartidaDAO detalleRepo;
    private final DocumentoService documentoService;

    public PartidaService(PartidaDAO p, DetallePartidaDAO d, DocumentoService doc) {
        this.partidaRepo = p; this.detalleRepo = d; this.documentoService = doc;
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
}
