package cl.digitalfix.catalog.dto.response;

import java.math.BigDecimal;

public record ServicioResponse(Long id, String nombre, String descripcion, BigDecimal tarifa) {}
