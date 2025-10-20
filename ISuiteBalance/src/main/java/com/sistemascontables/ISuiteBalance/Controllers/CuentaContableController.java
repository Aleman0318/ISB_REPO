package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.CuentaContable;
import com.sistemascontables.ISuiteBalance.Repositorios.CuentaContableDAO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaContableController {

    private final CuentaContableDAO dao;

    public CuentaContableController(CuentaContableDAO dao) {
        this.dao = dao;  // ✅ evita "Variable 'dao' might not have been initialized"
    }

    /**
     * Lista todas las cuentas ordenadas por código (ideal para poblar el combo).
     * GET /api/cuentas
     */
    @GetMapping
    public List<CuentaContable> listar() {
        // Asegúrate de tener este método en tu DAO:
        // List<CuentaContable> findAllByOrderByCodigoAsc();
        return dao.findAllByOrderByCodigoAsc();
    }

    /**
     * Obtiene una cuenta por su id.
     * GET /api/cuentas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CuentaContable> obtener(@PathVariable Long id) {
        return dao.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
