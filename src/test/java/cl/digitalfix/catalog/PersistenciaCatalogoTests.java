package cl.digitalfix.catalog;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import cl.digitalfix.catalog.dto.request.ServicioRequest;
import cl.digitalfix.catalog.service.ServicioService;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersistenciaCatalogoTests {
    @Autowired ServicioService servicio;

    @Test
    void guardaYConsultaCatalogoConJpaReal() {
        var nuevo = new ServicioRequest();
        nuevo.setNombre("Mantención");
        nuevo.setDescripcion("Revisión eléctrica");
        nuevo.setTarifa(new BigDecimal("25000.00"));
        var guardado = servicio.crearServicio(nuevo);
        assertNotNull(guardado.id());
        var leido = servicio.listarServicios().stream()
            .filter(s -> s.id().equals(guardado.id())).findFirst().orElseThrow();
        assertEquals("Mantención", leido.nombre());
        assertEquals(0, new BigDecimal("25000.00").compareTo(leido.tarifa()));
    }
}
