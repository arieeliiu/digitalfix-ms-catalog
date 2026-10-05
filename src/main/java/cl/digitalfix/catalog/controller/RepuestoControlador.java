package cl.digitalfix.catalog.controller;
import cl.digitalfix.catalog.dto.request.DescontarStockSolicitud;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.digitalfix.catalog.dto.request.RepuestoSolicitud;
import cl.digitalfix.catalog.dto.response.RepuestoResponse;
import cl.digitalfix.catalog.service.RepuestoServicio;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalog/spare-parts")
public class RepuestoControlador {

    private final RepuestoServicio repuestoServicio;

    public RepuestoControlador(RepuestoServicio repuestoServicio) {
        this.repuestoServicio = repuestoServicio;
    }

    @GetMapping
    public List<RepuestoResponse> listarRepuestos() {
        return repuestoServicio.listarRepuestos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepuestoResponse crearRepuesto(@Valid @RequestBody RepuestoSolicitud repuesto) {
        return repuestoServicio.crearRepuesto(repuesto);
    }

    @PutMapping("/{id}")
    public RepuestoResponse actualizarRepuesto(
            @PathVariable Long id,
            @Valid @RequestBody RepuestoSolicitud repuesto) {

        return repuestoServicio.actualizarRepuesto(id, repuesto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarRepuesto(@PathVariable Long id) {
        repuestoServicio.eliminarRepuesto(id);
    }

    @PostMapping("/discount-stock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void descontarStock(
            @Valid @RequestBody DescontarStockSolicitud solicitud) {

        repuestoServicio.descontarStock(solicitud);
    }

}
