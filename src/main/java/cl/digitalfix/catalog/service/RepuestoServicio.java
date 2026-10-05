package cl.digitalfix.catalog.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.digitalfix.catalog.dto.request.DescontarStockSolicitud;
import cl.digitalfix.catalog.dto.request.RepuestoStockSolicitud;
import cl.digitalfix.catalog.entity.DescuentoStockOrden;
import cl.digitalfix.catalog.entity.Repuesto;
import cl.digitalfix.catalog.dto.request.RepuestoSolicitud;
import cl.digitalfix.catalog.dto.response.RepuestoResponse;
import cl.digitalfix.catalog.mapper.RepuestoMapper;
import cl.digitalfix.catalog.exception.RecursoNoEncontradoException;
import cl.digitalfix.catalog.repository.DescuentoStockOrdenRepositorio;
import cl.digitalfix.catalog.repository.RepuestoRepositorio;

@Service
public class RepuestoServicio {

    private final RepuestoRepositorio repositorio;
    private final DescuentoStockOrdenRepositorio descuentoStockRepositorio;

    public RepuestoServicio(
            RepuestoRepositorio repositorio,
            DescuentoStockOrdenRepositorio descuentoStockRepositorio) {

        this.repositorio = repositorio;
        this.descuentoStockRepositorio = descuentoStockRepositorio;
    }

    public List<RepuestoResponse> listarRepuestos() {
        return repositorio.findAll().stream().map(RepuestoMapper::respuesta).toList();
    }

    public RepuestoResponse crearRepuesto(RepuestoSolicitud solicitud) {
        var repuesto = RepuestoMapper.entidad(solicitud);
        return RepuestoMapper.respuesta(repositorio.save(repuesto));
    }

    public RepuestoResponse actualizarRepuesto(Long id, RepuestoSolicitud datosActualizados) {
        Repuesto repuesto = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un repuesto con id " + id));

        repuesto.setNombre(datosActualizados.getNombre());
        repuesto.setDescripcion(datosActualizados.getDescripcion());
        repuesto.setStock(datosActualizados.getStock());

        return RepuestoMapper.respuesta(repositorio.save(repuesto));
    }

    public void eliminarRepuesto(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "No existe un repuesto con id " + id);
        }

        repositorio.deleteById(id);
    }

    @Transactional
    public void descontarStock(DescontarStockSolicitud solicitud) {

        Map<Long, Integer> cantidadesSolicitadas = solicitud.repuestos()
                .stream()
                .collect(Collectors.toMap(
                        RepuestoStockSolicitud::repuestoId,
                        RepuestoStockSolicitud::cantidad,
                        Integer::sum,
                        TreeMap::new));

        String firmaSolicitud = cantidadesSolicitadas.entrySet()
                .stream()
                .map(entrada -> entrada.getKey() + ":" + entrada.getValue())
                .collect(Collectors.joining("|"));

        var descuentoExistente =
                descuentoStockRepositorio.findById(solicitud.ordenId());

        if (descuentoExistente.isPresent()) {

            if (!descuentoExistente.get()
                    .getFirmaSolicitud()
                    .equals(firmaSolicitud)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La orden ya desconto stock con repuestos diferentes");
            }

            return;
        }

        List<Repuesto> repuestos = repositorio.buscarTodosParaActualizar(
                cantidadesSolicitadas.keySet());

        Map<Long, Repuesto> repuestosPorId = repuestos.stream()
                .collect(Collectors.toMap(
                        Repuesto::getId,
                        Function.identity()));

        for (Long repuestoId : cantidadesSolicitadas.keySet()) {

            Repuesto repuesto = repuestosPorId.get(repuestoId);

            if (repuesto == null) {
                throw new RecursoNoEncontradoException(
                        "No existe un repuesto con id " + repuestoId);
            }

            int cantidadSolicitada = cantidadesSolicitadas.get(repuestoId);

            if (repuesto.getStock() < cantidadSolicitada) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para el repuesto " + repuestoId);
            }
        }

        for (Repuesto repuesto : repuestos) {
            int cantidadSolicitada =
                    cantidadesSolicitadas.get(repuesto.getId());

            repuesto.setStock(
                    repuesto.getStock() - cantidadSolicitada);
        }

        repositorio.saveAll(repuestos);

        descuentoStockRepositorio.save(
            new DescuentoStockOrden(
                    solicitud.ordenId(),
                    firmaSolicitud));
    }
}
