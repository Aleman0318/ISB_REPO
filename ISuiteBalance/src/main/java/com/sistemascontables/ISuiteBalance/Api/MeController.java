package com.sistemascontables.ISuiteBalance.Api;

import com.sistemascontables.ISuiteBalance.Models.Usuario;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class MeController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        // No autenticado ⇒ Invitado
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.ok(Map.of(
                    "nombre", "Invitado",
                    "correo", null,
                    "rol",    "Invitado"
            ));
        }

        String correo = auth.getName(); // username = correo
        Optional<Usuario> opt = usuarioService.findByCorreo(correo);

        if (opt.isPresent()) {
            Usuario u = opt.get();
            return ResponseEntity.ok(Map.of(
                    "nombre", u.getNombre(),
                    "correo", u.getCorreo(),
                    "rol",    u.getRol()   // "Administrador" | "Contador" | "Auditor" | "Invitado"
            ));
        }

        // Fallback si por alguna razón no está en BD
        return ResponseEntity.ok(Map.of(
                "nombre", correo,
                "correo", correo,
                "rol",    "Invitado"
        ));
    }
}
