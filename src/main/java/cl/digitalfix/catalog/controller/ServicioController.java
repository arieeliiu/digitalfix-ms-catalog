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

import cl.digitalfix.catalog.dto.request.ServicioRequest;
import cl.digitalfix.catalog.dto.response.ServicioResponse;
import cl.digitalfix.catalog.service.ServicioService;

@RestController
@RequestMapping("/api/catalog/services")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioCatalogo;

    @GetMapping
    public List<ServicioResponse> listarServicios() {
        return servicioCatalogo.listarServicios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioResponse crearServicio(@Valid @RequestBody ServicioRequest servicio) {
        return servicioCatalogo.crearServicio(servicio);
    }

    @PutMapping("/{id}")
    public ServicioResponse actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioRequest servicio) {

        return servicioCatalogo.actualizarServicio(id, servicio);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarServicio(@PathVariable Long id) {
        servicioCatalogo.eliminarServicio(id);
    }
}
