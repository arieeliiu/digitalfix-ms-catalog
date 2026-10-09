package cl.digitalfix.catalog.mapper;

import cl.digitalfix.catalog.dto.request.ServicioRequest;
import cl.digitalfix.catalog.dto.response.ServicioResponse;
import cl.digitalfix.catalog.entity.ServicioCatalogo;

public final class ServicioMapper {
    private ServicioMapper() {}

    public static ServicioCatalogo entidad(ServicioRequest solicitud) {
        var entidad = new ServicioCatalogo();
        entidad.setNombre(solicitud.getNombre());
        entidad.setDescripcion(solicitud.getDescripcion());
        entidad.setTarifa(solicitud.getTarifa());
        return entidad;
    }

    public static ServicioResponse respuesta(ServicioCatalogo entidad) {
        return new ServicioResponse(entidad.getId(), entidad.getNombre(),
                entidad.getDescripcion(), entidad.getTarifa());
    }
}
