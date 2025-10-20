package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.DocumentoFuente;
import com.sistemascontables.ISuiteBalance.Repositorios.DocumentoFuenteDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
public class DocumentoService {

    private final Path docsRoot;
    private final DocumentoFuenteDAO repo;
    // Id por defecto para “OTRO” (ajústalo al que creaste en DB)
    private final Integer defaultClasificacionId;

    public DocumentoService(@Value("${app.docs-dir}") String docsDir,
                            @Value("${app.default-doc-classif:4}") Integer defaultClasificacionId,
                            DocumentoFuenteDAO repo) throws IOException {
        this.docsRoot = Paths.get(docsDir).toAbsolutePath().normalize();
        Files.createDirectories(this.docsRoot);
        this.repo = repo;
        this.defaultClasificacionId = defaultClasificacionId;
    }

    // ÚNICO flujo de guardado: siempre setea idClasificacion ANTES de insertar
    @Transactional
    public DocumentoFuente guardarPDF(MultipartFile file, Integer idClasificacion) throws IOException {
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (file.isEmpty() || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException("Debe subir un PDF válido");
        }

        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "documento.pdf");
        String safeName = UUID.randomUUID() + "-" + original.replaceAll("\\s+", "_");

        Path destino = docsRoot.resolve(safeName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        DocumentoFuente doc = new DocumentoFuente();
        doc.setNombreArchivo(safeName);
        doc.setFecha(LocalDate.now());
        doc.setTipo("PDF");
        // usa el que venga; si no viene, usa 99 (OTRO) o el que definas como default
        doc.setIdClasificacion(idClasificacion != null ? idClasificacion : 99);

        return repo.save(doc);
    }


    // Si algún código antiguo llama esta sobrecarga, delega con null (usará el default)
    @Transactional
    public DocumentoFuente guardarPDF(MultipartFile file) throws IOException {
        return guardarPDF(file, null);
    }

    @Transactional
    public void vincularADPartida(Long idDocumento, Long idPartida) {
        DocumentoFuente d = repo.findById(idDocumento).orElseThrow();
        d.setIdPartida(idPartida);
        repo.save(d);
    }

    @Transactional
    public DocumentoFuente reemplazarPDFParaPartida(Long idPartida, MultipartFile file, Integer idClasificacion) throws IOException {
        var docOpt = repo.findByIdPartida(idPartida);
        if (docOpt.isEmpty()) {
            // Si por alguna razón no hay doc, usa el flujo normal + vincula:
            DocumentoFuente nuevo = guardarPDF(file, idClasificacion);
            nuevo.setIdPartida(idPartida);
            return repo.save(nuevo);
        }

        DocumentoFuente actual = docOpt.get();

        // Borrar archivo anterior si existe
        if (actual.getNombreArchivo() != null && !actual.getNombreArchivo().isBlank()) {
            Path viejo = docsRoot.resolve(actual.getNombreArchivo());
            try { Files.deleteIfExists(viejo); } catch (Exception ignored) {}
        }

        // Validar y guardar nuevo PDF (reutilizando reglas)
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (file.isEmpty() || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException("Debe subir un PDF válido");
        }

        String original = java.util.Objects.requireNonNullElse(file.getOriginalFilename(), "documento.pdf");
        String safeName = java.util.UUID.randomUUID() + "-" + original.replaceAll("\\s+", "_");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, docsRoot.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        }

        actual.setNombreArchivo(safeName);
        actual.setFecha(java.time.LocalDate.now());
        actual.setTipo("PDF");
        actual.setIdClasificacion(idClasificacion != null ? idClasificacion : this.defaultClasificacionId);

        return repo.save(actual);
    }

}
