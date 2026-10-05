package cl.digitalfix.catalog.mapper;

import cl.digitalfix.catalog.entity.ServicioCatalogo;
import cl.digitalfix.catalog.dto.request.ServicioCatalogoSolicitud;
import cl.digitalfix.catalog.dto.response.ServicioCatalogoResponse;

public final class ServicioCatalogoMapper {
    private ServicioCatalogoMapper() {}

    public static ServicioCatalogo entidad(ServicioCatalogoSolicitud solicitud) {
        var entidad = new ServicioCatalogo();
        entidad.setNombre(solicitud.getNombre());
        entidad.setDescripcion(solicitud.getDescripcion());
        entidad.setTarifa(solicitud.getTarifa());
        return entidad;
    }

    public static ServicioCatalogoResponse respuesta(ServicioCatalogo entidad) {
        return new ServicioCatalogoResponse(entidad.getId(), entidad.getNombre(),
                entidad.getDescripcion(), entidad.getTarifa());
    }
}
