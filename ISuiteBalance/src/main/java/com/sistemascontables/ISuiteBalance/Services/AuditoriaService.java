package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.Auditoria;
import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Repositorios.AuditoriaDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.UsuarioDAO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaDAO auditoriaDAO;
    private final UsuarioDAO usuarioDAO;

    public AuditoriaService(AuditoriaDAO auditoriaDAO, UsuarioDAO usuarioDAO) {
        this.auditoriaDAO = auditoriaDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public void registrarAccion(Long idUsuarioActor,
                                String accion,
                                String entidad,
                                String descripcion) {

        Usuario usuario = usuarioDAO.findById(idUsuarioActor)
                .orElse(null); // si no lo encuentra, se guarda null, opcionalmente puedes exigir que exista

        Auditoria a = new Auditoria();
        a.setFecha(LocalDateTime.now());
        a.setAccion(accion);
        a.setEntidadAfectada(entidad);
        a.setDescripcion(descripcion);
        a.setUsuario(usuario);

        auditoriaDAO.save(a);
    }
}
