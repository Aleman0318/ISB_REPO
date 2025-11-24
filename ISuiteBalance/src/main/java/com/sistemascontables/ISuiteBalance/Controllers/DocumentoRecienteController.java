package com.sistemascontables.ISuiteBalance.Controllers;

import com.sistemascontables.ISuiteBalance.Services.DocumentoRecienteService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
@RequestMapping("/docs-recientes")
public class DocumentoRecienteController {

    private final DocumentoRecienteService documentoRecienteService;

    public DocumentoRecienteController(DocumentoRecienteService documentoRecienteService) {
        this.documentoRecienteService = documentoRecienteService;
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> verDocumento(@PathVariable String filename) throws IOException {

        Resource recurso = documentoRecienteService.cargarComoRecurso(filename);
        if (recurso == null || !recurso.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType;
        try {
            contentType = Files.probeContentType(Paths.get(recurso.getFile().getAbsolutePath()));
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }
}
