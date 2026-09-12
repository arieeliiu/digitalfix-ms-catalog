package cl.digitalfix.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.digitalfix.catalog.entity.ServicioCatalogo;

public interface ServicioCatalogoRepositorio extends JpaRepository<ServicioCatalogo, Long> {
}