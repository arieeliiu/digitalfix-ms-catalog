package cl.digitalfix.catalog.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "descuentos_stock_orden")
public class DescuentoStockOrden {

    @Id
    @Column(name = "orden_id")
    private Long ordenId;

    @Column(name = "firma_solicitud", nullable = false, length = 2000)
    private String firmaSolicitud;

    @Column(name = "fecha_descuento", nullable = false)
    private LocalDateTime fechaDescuento;

    protected DescuentoStockOrden() {
        // Constructor requerido por JPA.
    }

    public DescuentoStockOrden(Long ordenId, String firmaSolicitud) {
        this.ordenId = ordenId;
        this.firmaSolicitud = firmaSolicitud;
        this.fechaDescuento = LocalDateTime.now();
    }

    public Long getOrdenId() {
        return ordenId;
    }

    public String getFirmaSolicitud() {
        return firmaSolicitud;
    }

    public LocalDateTime getFechaDescuento() {
        return fechaDescuento;
    }
}