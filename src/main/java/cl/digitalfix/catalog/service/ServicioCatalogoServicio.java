package cl.digitalfix.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.digitalfix.catalog.entity.ServicioCatalogo;
import cl.digitalfix.catalog.dto.request.ServicioCatalogoSolicitud;
import cl.digitalfix.catalog.dto.response.ServicioCatalogoResponse;
import cl.digitalfix.catalog.mapper.ServicioCatalogoMapper;
import cl.digitalfix.catalog.exception.RecursoNoEncontradoException;
import cl.digitalfix.catalog.repository.ServicioCatalogoRepositorio;

@Service
public class ServicioCatalogoServicio {

    private final ServicioCatalogoRepositorio repositorio;

    public ServicioCatalogoServicio(ServicioCatalogoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<ServicioCatalogoResponse> listarServicios() {
        return repositorio.findAll().stream().map(ServicioCatalogoMapper::respuesta).toList();
    }

    public ServicioCatalogoResponse crearServicio(ServicioCatalogoSolicitud solicitud) {
        var servicio = ServicioCatalogoMapper.entidad(solicitud);
        return ServicioCatalogoMapper.respuesta(repositorio.save(servicio));
    }

    public ServicioCatalogoResponse actualizarServicio(Long id, ServicioCatalogoSolicitud datosActualizados) {
        ServicioCatalogo servicio = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un servicio con id " + id));

        servicio.setNombre(datosActualizados.getNombre());
        servicio.setDescripcion(datosActualizados.getDescripcion());
        servicio.setTarifa(datosActualizados.getTarifa());

        return ServicioCatalogoMapper.respuesta(repositorio.save(servicio));
    }

    public void eliminarServicio(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "No existe un servicio con id " + id);
        }

        repositorio.deleteById(id);
    }

}
