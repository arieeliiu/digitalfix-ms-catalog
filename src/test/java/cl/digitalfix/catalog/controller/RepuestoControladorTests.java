package cl.digitalfix.catalog.controller;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import cl.digitalfix.catalog.entity.Repuesto;
import cl.digitalfix.catalog.repository.RepuestoRepositorio;
import cl.digitalfix.catalog.repository.DescuentoStockOrdenRepositorio;
import cl.digitalfix.catalog.service.RepuestoServicio;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepuestoControlador.class)
@Import(RepuestoServicio.class)
class RepuestoControladorTests {
    @Autowired MockMvc cliente;
    @MockitoBean RepuestoRepositorio repositorio;
    @MockitoBean DescuentoStockOrdenRepositorio descuentos;

    @Test
    void omitirStockConservaCeroEIgnoraIdDelRequest() throws Exception {
        when(repositorio.save(any(Repuesto.class))).thenAnswer(llamada -> {
            Repuesto repuesto = llamada.getArgument(0);
            org.junit.jupiter.api.Assertions.assertNull(repuesto.getId());
            repuesto.setId(7L);
            return repuesto;
        });
        cliente.perform(post("/api/catalog/spare-parts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":99,\"nombre\":\"Interruptor\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Interruptor"))
                .andExpect(jsonPath("$.stock").value(0));
    }

    @Test
    void stockNullYNegativoConservanErrores() throws Exception {
        cliente.perform(post("/api/catalog/spare-parts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Interruptor\",\"stock\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.stock").value("El stock es obligatorio"));
        cliente.perform(post("/api/catalog/spare-parts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Interruptor\",\"stock\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.stock").value("El stock no puede ser negativo"));
        verifyNoInteractions(repositorio);
    }

    @Test
    void listarActualizarYEliminarMantienenContrato() throws Exception {
        var repuesto = new Repuesto();
        repuesto.setId(7L);
        repuesto.setNombre("Interruptor");
        repuesto.setDescripcion("Tablero");
        repuesto.setStock(20);
        when(repositorio.findAll()).thenReturn(List.of(repuesto));
        when(repositorio.findById(7L)).thenReturn(Optional.of(repuesto));
        when(repositorio.save(any(Repuesto.class))).thenAnswer(llamada -> llamada.getArgument(0));
        when(repositorio.existsById(7L)).thenReturn(true);
        cliente.perform(get("/api/catalog/spare-parts"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].stock").value(20));
        cliente.perform(put("/api/catalog/spare-parts/7").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":99,\"nombre\":\"Nuevo\",\"descripcion\":\"Tablero\",\"stock\":15}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Nuevo"))
                .andExpect(jsonPath("$.descripcion").value("Tablero"))
                .andExpect(jsonPath("$.stock").value(15));
        cliente.perform(delete("/api/catalog/spare-parts/7")).andExpect(status().isNoContent());
        verify(repositorio).deleteById(7L);
    }
}
