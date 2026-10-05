package cl.digitalfix.catalog.mapper;

import cl.digitalfix.catalog.entity.Repuesto;
import cl.digitalfix.catalog.dto.request.RepuestoSolicitud;
import cl.digitalfix.catalog.dto.response.RepuestoResponse;

public final class RepuestoMapper {
    private RepuestoMapper() {}

    public static Repuesto entidad(RepuestoSolicitud solicitud) {
        var entidad = new Repuesto();
        entidad.setNombre(solicitud.getNombre());
        entidad.setDescripcion(solicitud.getDescripcion());
        entidad.setStock(solicitud.getStock());
        return entidad;
    }

    public static RepuestoResponse respuesta(Repuesto entidad) {
        return new RepuestoResponse(entidad.getId(), entidad.getNombre(),
                entidad.getDescripcion(), entidad.getStock());
    }
}
