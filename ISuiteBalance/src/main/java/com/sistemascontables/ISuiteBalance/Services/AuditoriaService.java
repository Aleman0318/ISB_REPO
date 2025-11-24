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

    // 👇 Constructor: Spring inyecta los repos
    public AuditoriaService(AuditoriaDAO auditoriaDAO,
                            UsuarioDAO usuarioDAO) {
        this.auditoriaDAO = auditoriaDAO;
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Registra una acción en la tabla tbl_auditoria.
     *
     * @param idUsuarioActor id del usuario que realiza la acción
     * @param accion         texto corto de la acción (CREAR_REPORTE, APROBAR_REPORTE, etc.)
     * @param entidad        entidad afectada (por ej. "REPORTE")
     * @param descripcion    descripción larga que verás en la bitácora
     */
    public void registrarAccion(Long idUsuarioActor,
                                String accion,
                                String entidad,
                                String descripcion) {

        Usuario usuario = null;
        if (idUsuarioActor != null) {
            usuario = usuarioDAO.findById(idUsuarioActor)
                    .orElse(null); // si no lo encuentra, se guarda null
        }

        Auditoria a = new Auditoria();
        a.setFecha(LocalDateTime.now());
        a.setAccion(accion);
        a.setEntidadAfectada(entidad);
        a.setDescripcion(descripcion);
        a.setUsuario(usuario);

        auditoriaDAO.save(a);
    }

    // (Opcional) overload si alguna vez quieres pasar el Usuario directamente
    public void registrarAccion(Usuario actor,
                                String accion,
                                String entidad,
                                String descripcion) {
        Long id = (actor != null) ? actor.getId_usuario() : null;
        registrarAccion(id, accion, entidad, descripcion);
    }
}
