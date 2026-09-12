package cl.digitalfix.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.digitalfix.catalog.entity.ServicioCatalogo;
import cl.digitalfix.catalog.service.ServicioCatalogoServicio;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalog/services")
public class ServicioCatalogoControlador {

    private final ServicioCatalogoServicio servicioCatalogo;

    public ServicioCatalogoControlador(ServicioCatalogoServicio servicioCatalogo) {
        this.servicioCatalogo = servicioCatalogo;
    }

    @GetMapping
    public List<ServicioCatalogo> listarServicios() {
        return servicioCatalogo.listarServicios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioCatalogo crearServicio(@Valid @RequestBody ServicioCatalogo servicio) {
        return servicioCatalogo.crearServicio(servicio);
    }

    @PutMapping("/{id}")
    public ServicioCatalogo actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioCatalogo servicio) {

        return servicioCatalogo.actualizarServicio(id, servicio);
    }
}