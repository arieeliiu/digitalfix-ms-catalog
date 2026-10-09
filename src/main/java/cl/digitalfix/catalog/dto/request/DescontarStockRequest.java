package cl.digitalfix.catalog.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DescontarStockRequest(

    @NotNull(message = "La orden es obligatoria")
    @Positive(message = "El identificador de la orden debe ser positivo")
    Long ordenId,

    @NotEmpty(message = "Debe indicar al menos un repuesto")
    List<@Valid RepuestoStockRequest> repuestos

) {}
