package cl.digitalfix.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import cl.digitalfix.catalog.entity.DescuentoStockOrden;

public interface DescuentoStockOrdenRepository
        extends JpaRepository<DescuentoStockOrden, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DescuentoStockOrden d where d.ordenId = :id")
    Optional<DescuentoStockOrden> buscarParaActualizar(Long id);
}
