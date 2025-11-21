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

    // 🔍 Registrar acción sobre REPORTES
    public void registrarAccionReporte(Long idUsuarioActor,
                                       String accion,
                                       String descripcion) {

        Usuario usuario = usuarioDAO.findById(idUsuarioActor)
                .orElse(null); // si no lo encontrás, se guarda null

        Auditoria a = new Auditoria();
        a.setFecha(LocalDateTime.now());
        a.setAccion(accion);                 // p.ej: CREAR_REPORTE, APROBAR_REPORTE, RECHAZAR_REPORTE
        a.setEntidadAfectada("REPORTE");     // 👈 clave para filtrar en bitácora
        a.setDescripcion(descripcion);       // texto libre
        a.setUsuario(usuario);

        auditoriaDAO.save(a);
    }
}
