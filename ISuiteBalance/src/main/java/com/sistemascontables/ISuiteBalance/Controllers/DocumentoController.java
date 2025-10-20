// DocumentoController.java
package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Models.DocumentoFuente;
import com.sistemascontables.ISuiteBalance.Repositorios.ClasificacionDocumentoDAO;
import com.sistemascontables.ISuiteBalance.Repositorios.DocumentoFuenteDAO;
import com.sistemascontables.ISuiteBalance.Services.DocumentoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.io.IOException;
import java.util.Map;


@Controller
public class DocumentoController {

    private final DocumentoService documentoService;
    private final ClasificacionDocumentoDAO clasifRepo;
    private final DocumentoFuenteDAO documentoRepo;

    @Value("${app.docs-dir}")
    private String docsDir;

    public DocumentoController(DocumentoService documentoService,
                               ClasificacionDocumentoDAO clasifRepo, DocumentoFuenteDAO documentoRepo) {
        this.documentoService = documentoService;
        this.clasifRepo = clasifRepo;
        this.documentoRepo = documentoRepo;
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

    // ver/descargar documento por id de partida
    @GetMapping("/documento/partida/{idPartida}")
    public ResponseEntity<Resource> verDocumentoDePartida(@PathVariable Long idPartida) {
        Optional<DocumentoFuente> opt = documentoRepo.findByIdPartida(idPartida);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        DocumentoFuente doc = opt.get();
        try {
            Path path = Paths.get(docsDir).resolve(doc.getNombreArchivo());
            Resource file = new UrlResource(path.toUri());
            if (!file.exists()) return ResponseEntity.notFound().build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getNombreArchivo() + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(file);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
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

