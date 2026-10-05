package cl.digitalfix.catalog.dto.response;

import java.math.BigDecimal;

public record ServicioCatalogoResponse(Long id, String nombre, String descripcion, BigDecimal tarifa) {}
