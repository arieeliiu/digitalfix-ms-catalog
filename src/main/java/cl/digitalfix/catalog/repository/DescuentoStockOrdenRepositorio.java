package cl.digitalfix.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.digitalfix.catalog.entity.DescuentoStockOrden;

public interface DescuentoStockOrdenRepositorio
        extends JpaRepository<DescuentoStockOrden, Long> {
}