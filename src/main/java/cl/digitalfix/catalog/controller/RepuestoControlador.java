package cl.digitalfix.catalog.controller;

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

import cl.digitalfix.catalog.entity.Repuesto;
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
    public List<Repuesto> listarRepuestos() {
        return repuestoServicio.listarRepuestos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Repuesto crearRepuesto(@Valid @RequestBody Repuesto repuesto) {
        return repuestoServicio.crearRepuesto(repuesto);
    }

    @PutMapping("/{id}")
    public Repuesto actualizarRepuesto(
            @PathVariable Long id,
            @Valid @RequestBody Repuesto repuesto) {

        return repuestoServicio.actualizarRepuesto(id, repuesto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarRepuesto(@PathVariable Long id) {
        repuestoServicio.eliminarRepuesto(id);
    }
}