package cl.digitalfix.catalog.dto.request;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Compara la reserva conocida por Workorders antes de cambiar el inventario.
public record AjustarStockOrdenRequest(
        @NotNull @Positive Long ordenId,
        @NotNull List<@Valid RepuestoStockRequest> anteriores,
        @NotNull List<@Valid RepuestoStockRequest> nuevos) {}
