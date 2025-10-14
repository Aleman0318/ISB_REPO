// PartidaController.java
package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.PartidaRequest;
import com.sistemascontables.ISuiteBalance.Repositorios.DocumentoFuenteDAO;
import com.sistemascontables.ISuiteBalance.Services.PartidaService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class PartidaController {
    private final PartidaService partidaService;
    private final DocumentoFuenteDAO docRepo;

    public PartidaController(PartidaService s, DocumentoFuenteDAO dr) {
        this.partidaService = s; this.docRepo = dr;
    }

    @GetMapping("/partida")
    public String pantallaPartida(@RequestParam(value="doc", required=false) Long docId, Model model){
        if (docId != null) {
            docRepo.findById(docId).ifPresent(d -> {
                model.addAttribute("docId", d.getId());
                model.addAttribute("docNombre", d.getNombreArchivo());
            });
        }
        return "RegistroPartida";
    }

    @PostMapping(value="/partida", consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> guardar(@RequestParam("docId") Long docId, @RequestBody PartidaRequest req){
        Long id = partidaService.guardarPartida(req, docId);
        return Map.of("ok", true, "idPartida", id);
    }
}
