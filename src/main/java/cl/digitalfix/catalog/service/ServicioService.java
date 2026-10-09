package cl.digitalfix.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import cl.digitalfix.catalog.dto.request.ServicioRequest;
import cl.digitalfix.catalog.dto.response.ServicioResponse;
import cl.digitalfix.catalog.entity.ServicioCatalogo;
import cl.digitalfix.catalog.exception.RecursoNoEncontradoException;
import cl.digitalfix.catalog.mapper.ServicioMapper;
import cl.digitalfix.catalog.repository.ServicioRepository;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository repositorio;

    public List<ServicioResponse> listarServicios() {
        return repositorio.findAll().stream().map(ServicioMapper::respuesta).toList();
    }

    public ServicioResponse crearServicio(ServicioRequest solicitud) {
        var servicio = ServicioMapper.entidad(solicitud);
        return ServicioMapper.respuesta(repositorio.save(servicio));
    }

    public ServicioResponse actualizarServicio(Long id, ServicioRequest datosActualizados) {
        ServicioCatalogo servicio = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un servicio con id " + id));

        servicio.setNombre(datosActualizados.getNombre());
        servicio.setDescripcion(datosActualizados.getDescripcion());
        servicio.setTarifa(datosActualizados.getTarifa());

        return ServicioMapper.respuesta(repositorio.save(servicio));
    }

    public void eliminarServicio(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "No existe un servicio con id " + id);
        }

        repositorio.deleteById(id);
    }

}
