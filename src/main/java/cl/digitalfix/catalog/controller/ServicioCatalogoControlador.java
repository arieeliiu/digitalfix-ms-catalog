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
import org.springframework.web.bind.annotation.DeleteMapping;

import cl.digitalfix.catalog.dto.request.ServicioCatalogoSolicitud;
import cl.digitalfix.catalog.dto.response.ServicioCatalogoResponse;
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
    public List<ServicioCatalogoResponse> listarServicios() {
        return servicioCatalogo.listarServicios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioCatalogoResponse crearServicio(@Valid @RequestBody ServicioCatalogoSolicitud servicio) {
        return servicioCatalogo.crearServicio(servicio);
    }

    @PutMapping("/{id}")
    public ServicioCatalogoResponse actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioCatalogoSolicitud servicio) {

        return servicioCatalogo.actualizarServicio(id, servicio);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarServicio(@PathVariable Long id) {
        servicioCatalogo.eliminarServicio(id);
    }
}
