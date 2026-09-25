package cl.digitalfix.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.digitalfix.catalog.entity.Repuesto;

public interface RepuestoRepositorio extends JpaRepository<Repuesto, Long> {
}