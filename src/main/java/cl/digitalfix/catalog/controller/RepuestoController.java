package cl.digitalfix.catalog.controller;

import java.util.List;

import jakarta.validation.Valid;

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

import lombok.RequiredArgsConstructor;

import cl.digitalfix.catalog.dto.request.DescontarStockRequest;
import cl.digitalfix.catalog.dto.request.RepuestoRequest;
import cl.digitalfix.catalog.dto.response.RepuestoResponse;
import cl.digitalfix.catalog.service.RepuestoService;

@RestController
@RequestMapping("/api/catalog/spare-parts")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService repuestoServicio;

    @GetMapping
    public List<RepuestoResponse> listarRepuestos() {
        return repuestoServicio.listarRepuestos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepuestoResponse crearRepuesto(@Valid @RequestBody RepuestoRequest repuesto) {
        return repuestoServicio.crearRepuesto(repuesto);
    }

    @PutMapping("/{id}")
    public RepuestoResponse actualizarRepuesto(
            @PathVariable Long id,
            @Valid @RequestBody RepuestoRequest repuesto) {

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
            @Valid @RequestBody DescontarStockRequest solicitud) {

        repuestoServicio.descontarStock(solicitud);
    }

}
