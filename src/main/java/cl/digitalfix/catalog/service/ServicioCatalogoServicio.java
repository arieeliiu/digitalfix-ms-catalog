package cl.digitalfix.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.digitalfix.catalog.entity.ServicioCatalogo;
import cl.digitalfix.catalog.exception.RecursoNoEncontradoException;
import cl.digitalfix.catalog.repository.ServicioCatalogoRepositorio;

@Service
public class ServicioCatalogoServicio {

    private final ServicioCatalogoRepositorio repositorio;

    public ServicioCatalogoServicio(ServicioCatalogoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<ServicioCatalogo> listarServicios() {
        return repositorio.findAll();
    }

    public ServicioCatalogo crearServicio(ServicioCatalogo servicio) {
        servicio.setId(null);
        return repositorio.save(servicio);
    }

    public ServicioCatalogo actualizarServicio(Long id, ServicioCatalogo datosActualizados) {
        ServicioCatalogo servicio = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un servicio con id " + id));

        servicio.setNombre(datosActualizados.getNombre());
        servicio.setDescripcion(datosActualizados.getDescripcion());
        servicio.setTarifa(datosActualizados.getTarifa());
          
        return repositorio.save(servicio);
    }

    public void eliminarServicio(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "No existe un servicio con id " + id);
        }

        repositorio.deleteById(id);
    }

}