package cl.digitalfix.catalog;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import cl.digitalfix.catalog.entity.ServicioCatalogo;
import cl.digitalfix.catalog.service.ServicioCatalogoServicio;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersistenciaCatalogoTests {
    @Autowired ServicioCatalogoServicio servicio;

    @Test
    void guardaYConsultaCatalogoConJpaReal() {
        var nuevo = new ServicioCatalogo();
        nuevo.setNombre("Mantención");
        nuevo.setDescripcion("Revisión eléctrica");
        nuevo.setTarifa(new BigDecimal("25000.00"));
        var guardado = servicio.crearServicio(nuevo);
        assertNotNull(guardado.getId());
        var leido = servicio.listarServicios().stream()
            .filter(s -> s.getId().equals(guardado.getId())).findFirst().orElseThrow();
        assertEquals("Mantención", leido.getNombre());
        assertEquals(0, new BigDecimal("25000.00").compareTo(leido.getTarifa()));
    }
}
