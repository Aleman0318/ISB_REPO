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

    @GetMapping("/partidas/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        var vm = partidaService.cargarParaEditar(id); // lanza si no existe
        model.addAttribute("modo", "EDIT"); // <- bandera dura de edición
        model.addAttribute("idPartida", vm.partida().getId());
        model.addAttribute("fecha", vm.partida().getFecha());
        model.addAttribute("concepto", vm.partida().getConcepto());
        model.addAttribute("lineas", vm.lineas());
        vm.documento().ifPresent(d -> {
            model.addAttribute("docNombre", d.getNombreArchivo());
            model.addAttribute("docClasif", d.getIdClasificacion());
        });
        return "PartidaEditar"; // Reusamos la misma vista PERO en modo EDIT
    }

    // Bloquea acceso a /partidas/editar (sin id)
    @GetMapping("/partidas/editar")
    public String editarSinId() {
        return "redirect:/gestion-partida";
    }

    @PostMapping("/partidas/{id}") // <- SOLO actualización; NO crea
    public String actualizar(@PathVariable Integer id,
                             @RequestParam("fecha") java.time.LocalDate fecha,
                             @RequestParam("concepto") String concepto,
                             @RequestParam("idCuenta[]") java.util.List<Long> idsCuenta,
                             @RequestParam("debe[]") java.util.List<java.math.BigDecimal> debes,
                             @RequestParam("haber[]") java.util.List<java.math.BigDecimal> haberes,
                             @RequestParam(value="pdf", required=false) org.springframework.web.multipart.MultipartFile pdf,
                             @RequestParam(value="idClasificacion", required=false) Integer idClasif,
                             org.springframework.ui.Model model) {
        try {
            partidaService.actualizarPartida(id, fecha, concepto, idsCuenta, debes, haberes, pdf, idClasif);
            return "redirect:/partidas/" + id + "/ver";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return editar(id, model); // re-render en modo EDIT
        }
    }

}
