package cl.digitalfix.catalog.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.digitalfix.catalog.entity.Repuesto;
import jakarta.persistence.LockModeType;

public interface RepuestoRepositorio extends JpaRepository<Repuesto, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from Repuesto r
            where r.id in :ids
            order by r.id
            """)
    List<Repuesto> buscarTodosParaActualizar(
            @Param("ids") Collection<Long> ids);
}