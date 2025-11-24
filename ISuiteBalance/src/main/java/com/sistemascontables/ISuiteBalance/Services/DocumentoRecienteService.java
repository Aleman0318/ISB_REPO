package com.sistemascontables.ISuiteBalance.Services;

import com.sistemascontables.ISuiteBalance.Models.DocumentoReciente;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DocumentoRecienteService {

    // Carpeta docs relativa al directorio de ejecución
    private final Path docsRoot = Paths.get("docs").toAbsolutePath().normalize();

    public DocumentoRecienteService() {
        System.out.println(">>> [DocumentoRecienteService] docsRoot = " + docsRoot);
        try {
            if (!Files.exists(docsRoot)) {
                System.out.println(">>> [DocumentoRecienteService] La carpeta docs no existe, se creará.");
                Files.createDirectories(docsRoot);
            }
        } catch (IOException e) {
            System.out.println(">>> [DocumentoRecienteService] Error creando carpeta docs: " + e.getMessage());
        }
    }

    public List<DocumentoReciente> listarRecientes(int limite) {
        try (Stream<Path> stream = Files.list(docsRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .sorted((p1, p2) -> {
                        try {
                            long t1 = Files.getLastModifiedTime(p1).toMillis();
                            long t2 = Files.getLastModifiedTime(p2).toMillis();
                            // más recientes primero
                            return Long.compare(t2, t1);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .limit(limite)
                    .map(p -> {
                        try {
                            LocalDateTime fecha = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(Files.getLastModifiedTime(p).toMillis()),
                                    ZoneId.systemDefault()
                            );
                            return new DocumentoReciente(p.getFileName().toString(), fecha);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(d -> d != null)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println(">>> [DocumentoRecienteService] Error listando docs: " + e.getMessage());
            return List.of();
        }
    }

    public Resource cargarComoRecurso(String filename) {
        try {
            Path filePath = docsRoot.resolve(filename).normalize();
            if (!filePath.startsWith(docsRoot) || !Files.exists(filePath)) {
                System.out.println(">>> [DocumentoRecienteService] Archivo no encontrado: " + filePath);
                return null;
            }
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            System.out.println(">>> [DocumentoRecienteService] Malformed URL: " + e.getMessage());
            return null;
        }
    }
}
