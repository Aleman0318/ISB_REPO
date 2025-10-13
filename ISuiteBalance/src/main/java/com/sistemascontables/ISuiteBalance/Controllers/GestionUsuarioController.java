package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GestionUsuarioController {

    private final UsuarioService usuarioService;

    public GestionUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

}
