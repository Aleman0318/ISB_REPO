// DocumentoController.java
package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.DocumentoFuente;
import com.sistemascontables.ISuiteBalance.Repositorios.ClasificacionDocumentoDAO;
import com.sistemascontables.ISuiteBalance.Services.DocumentoService;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
public class DocumentoController {

    private final DocumentoService documentoService;
    private final ClasificacionDocumentoDAO clasifRepo;

    public DocumentoController(DocumentoService documentoService,
                               ClasificacionDocumentoDAO clasifRepo) {
        this.documentoService = documentoService;
        this.clasifRepo = clasifRepo;
    }

    // GET /subir-doc -> manda la lista de clasificaciones al modelo para que Thymeleaf pinte el <select>
    @GetMapping("/subir-doc")
    public String subirDoc(Model model) {
        model.addAttribute("clasificaciones",
                clasifRepo.findAll(Sort.by(Sort.Direction.ASC, "idClasificacion")));
        return "SubirDoc"; // nombre de tu plantilla
    }

    // POST /documento/subir -> recibe archivo y (opcional) idClasificacion desde el form
    @PostMapping(value = "/documento/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, Object> subir(@RequestParam("archivo") MultipartFile archivo,
                                     @RequestParam(value = "idClasificacion", required = false) Integer idClasif) throws IOException {
        // Usa la sobrecarga que acepta idClasificacion (si es null, el service puede usar 99/trigger)
        DocumentoFuente d = documentoService.guardarPDF(archivo, idClasif);
        return Map.of("ok", true, "documentId", d.getId(), "fileName", d.getNombreArchivo());
    }

    // (Opcional) Fallback JSON para poblar el <select> vía fetch si no cargas por modelo
    @GetMapping("/documento/clasificaciones")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> listarClasificacionesJson() {
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (var c : clasifRepo.findAll(org.springframework.data.domain.Sort.by("idClasificacion"))) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("idClasificacion", c.getIdClasificacion());
            m.put("nombreclasificacion", c.getNombreclasificacion());
            out.add(m);
        }
        return out;
    }
}

