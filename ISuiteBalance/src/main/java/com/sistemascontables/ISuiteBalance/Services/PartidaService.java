package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.DetallePartida;
import com.sistemascontables.ISuiteBalance.Models.Partida;
import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.DetallePartidaView;
import com.sistemascontables.ISuiteBalance.Repositorios.PartidaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.PartidaResumen;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PartidaService {

    private final PartidaDAO partidaDAO;
    private final DetallePartidaDAO detalleDAO;

    public PartidaService(PartidaDAO partidaDAO, DetallePartidaDAO detalleDAO) {
        this.partidaDAO = partidaDAO;
        this.detalleDAO = detalleDAO;
    }

    public List<DetallePartidaView> obtenerLineasConNombre(Integer idPartida){
        return detalleDAO.listarLineasConNombre(idPartida);
    }

    public Page<PartidaResumen> listarResumen(int page, int size) {
        return partidaDAO.listarResumen(PageRequest.of(page, size));
    }

    public List<DetallePartida> obtenerLineas(Integer idPartida) {
        return detalleDAO.findByIdPartida(idPartida);
    }

    @Transactional
    public Partida crearPartida(Partida p, List<DetallePartida> lineas) {
        Partida guardada = partidaDAO.save(p);
        if (lineas != null) {
            for (DetallePartida l : lineas) {
                l.setIdPartida(guardada.getIdPartida());
                if (l.getMontoDebe() == null)  l.setMontoDebe(BigDecimal.ZERO);
                if (l.getMontoHaber() == null) l.setMontoHaber(BigDecimal.ZERO);
                detalleDAO.save(l);
            }
        }
        return guardada;
    }

    @Transactional
    public void actualizarPartida(Integer idPartida, Partida p, List<DetallePartida> lineas) {
        Partida existente = partidaDAO.findById(idPartida).orElseThrow();
        existente.setFecha(p.getFecha());
        existente.setConcepto(p.getConcepto());
        existente.setIdUsuario(p.getIdUsuario());
        partidaDAO.save(existente);

        detalleDAO.deleteByIdPartida(idPartida);
        if (lineas != null) {
            for (DetallePartida l : lineas) {
                l.setIdPartida(idPartida);
                if (l.getMontoDebe() == null)  l.setMontoDebe(BigDecimal.ZERO);
                if (l.getMontoHaber() == null) l.setMontoHaber(BigDecimal.ZERO);
                detalleDAO.save(l);
            }
        }
    }

    @Transactional
    public void eliminarPartida(Integer idPartida) {
        detalleDAO.deleteByIdPartida(idPartida); // hijos primero
        partidaDAO.deleteById(idPartida);        // luego la partida
    }
}
