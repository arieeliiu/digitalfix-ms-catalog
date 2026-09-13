package cl.digitalfix.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.digitalfix.catalog.entity.ServicioCatalogo;
import cl.digitalfix.catalog.exception.RecursoNoEncontradoException;
import cl.digitalfix.catalog.service.ServicioCatalogoServicio;

@WebMvcTest(ServicioCatalogoControlador.class)
class ServicioCatalogoControladorTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioCatalogoServicio servicioCatalogo;

    @Test
    void listarServiciosRetornaServicios() throws Exception {
        ServicioCatalogo servicio = crearServicioEjemplo();

        when(servicioCatalogo.listarServicios())
                .thenReturn(List.of(servicio));

        mockMvc.perform(get("/api/catalog/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Mantencion electrica"));
    }

    @Test
    void crearServicioRetornaCreado() throws Exception {
        ServicioCatalogo servicio = crearServicioEjemplo();

        when(servicioCatalogo.crearServicio(any(ServicioCatalogo.class)))
                .thenReturn(servicio);

        mockMvc.perform(post("/api/catalog/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nombre": "Mantencion electrica",
                          "descripcion": "Revision de instalacion",
                          "tarifa": 25000
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mantencion electrica"));
    }

    @Test
    void actualizarServicioRetornaServicioActualizado() throws Exception {
        ServicioCatalogo servicio = crearServicioEjemplo();
        servicio.setNombre("Mantencion preventiva");
        servicio.setTarifa(new BigDecimal("30000"));

        when(servicioCatalogo.actualizarServicio(
                eq(1L),
                any(ServicioCatalogo.class)))
                .thenReturn(servicio);

        mockMvc.perform(put("/api/catalog/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nombre": "Mantencion preventiva",
                          "descripcion": "Revision preventiva",
                          "tarifa": 30000
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mantencion preventiva"))
                .andExpect(jsonPath("$.tarifa").value(30000));
    }

    @Test
    void crearServicioInvalidoRetornaBadRequest() throws Exception {
        mockMvc.perform(post("/api/catalog/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nombre": "",
                          "descripcion": "Servicio invalido",
                          "tarifa": -100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.tarifa").value("La tarifa no puede ser negativa"));
    }

    @Test
    void actualizarServicioInexistenteRetornaNotFound() throws Exception {
        when(servicioCatalogo.actualizarServicio(
                eq(999L),
                any(ServicioCatalogo.class)))
                .thenThrow(new RecursoNoEncontradoException(
                        "No existe un servicio con id 999"));

        mockMvc.perform(put("/api/catalog/services/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nombre": "Mantencion electrica",
                          "descripcion": "Revision",
                          "tarifa": 25000
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje")
                        .value("No existe un servicio con id 999"));
    }

    // Crea un servicio reutilizable para las pruebas correctas.
    private ServicioCatalogo crearServicioEjemplo() {
        ServicioCatalogo servicio = new ServicioCatalogo();
        servicio.setId(1L);
        servicio.setNombre("Mantencion electrica");
        servicio.setDescripcion("Revision de instalacion");
        servicio.setTarifa(new BigDecimal("25000"));
        return servicio;
    }
}