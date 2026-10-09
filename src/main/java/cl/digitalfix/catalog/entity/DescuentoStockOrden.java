package cl.digitalfix.catalog.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "descuentos_stock_orden")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DescuentoStockOrden {

    @Id
    @Column(name = "orden_id")
    private Long ordenId;

    @Column(name = "firma_solicitud", nullable = false, length = 2000)
    private String firmaSolicitud;

    @Column(name = "fecha_descuento", nullable = false)
    private LocalDateTime fechaDescuento;

    public DescuentoStockOrden(Long ordenId, String firmaSolicitud) {
        this.ordenId = ordenId;
        this.firmaSolicitud = firmaSolicitud;
        this.fechaDescuento = LocalDateTime.now();
    }
}
