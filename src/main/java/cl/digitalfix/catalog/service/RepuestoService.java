package cl.digitalfix.catalog.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import cl.digitalfix.catalog.dto.request.DescontarStockRequest;
import cl.digitalfix.catalog.dto.request.AjustarStockOrdenRequest;
import cl.digitalfix.catalog.dto.request.RepuestoRequest;
import cl.digitalfix.catalog.dto.request.RepuestoStockRequest;
import cl.digitalfix.catalog.dto.response.RepuestoResponse;
import cl.digitalfix.catalog.entity.DescuentoStockOrden;
import cl.digitalfix.catalog.entity.Repuesto;
import cl.digitalfix.catalog.exception.RecursoNoEncontradoException;
import cl.digitalfix.catalog.mapper.RepuestoMapper;
import cl.digitalfix.catalog.repository.DescuentoStockOrdenRepository;
import cl.digitalfix.catalog.repository.RepuestoRepository;

@Service
@RequiredArgsConstructor
public class RepuestoService {

    private final RepuestoRepository repositorio;
    private final DescuentoStockOrdenRepository descuentoStockRepositorio;

    public List<RepuestoResponse> listarRepuestos() {
        return repositorio.findAll().stream().map(RepuestoMapper::respuesta).toList();
    }

    public RepuestoResponse crearRepuesto(RepuestoRequest solicitud) {
        var repuesto = RepuestoMapper.entidad(solicitud);
        return RepuestoMapper.respuesta(repositorio.save(repuesto));
    }

    public RepuestoResponse actualizarRepuesto(Long id, RepuestoRequest datosActualizados) {
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

    // Reconciliación idempotente: Catalog conserva una firma de la reserva por orden.
    // El caller proporciona la reserva anterior y la nueva para evitar sobrescrituras.
    @Transactional
    public void ajustarStock(AjustarStockOrdenRequest solicitud) {
        var previos = cantidades(solicitud.anteriores());
        var nuevos = cantidades(solicitud.nuevos());
        String anterior = firma(previos);
        String objetivo = firma(nuevos);
        var registro = descuentoStockRepositorio.buscarParaActualizar(solicitud.ordenId());
        String actual = registro.map(DescuentoStockOrden::getFirmaSolicitud).orElse("-");
        if (actual.equals(objetivo)) return; // Reintento: la diferencia ya fue aplicada.
        if (!actual.equals(anterior)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reserva de la orden cambió; actualiza los datos antes de reintentar");
        }

        Set<Long> ids = new TreeSet<>(previos.keySet());
        ids.addAll(nuevos.keySet());
        if (ids.isEmpty()) return;
        var afectados = repositorio.buscarTodosParaActualizar(ids);
        var porId = afectados.stream().collect(Collectors.toMap(Repuesto::getId, Function.identity()));
        var calculados = new HashMap<Long, Integer>();
        for (Long id : ids) {
            int antes = previos.getOrDefault(id, 0);
            int despues = nuevos.getOrDefault(id, 0);
            int diferencia = despues - antes;
            if (diferencia == 0) continue;
            var repuesto = porId.get(id);
            if (repuesto == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el repuesto " + id);
            }
            long disponible = (long) repuesto.getStock() - diferencia;
            if (disponible < 0 || disponible > Integer.MAX_VALUE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente o fuera de rango para el repuesto " + id);
            }
            calculados.put(id, (int) disponible);
        }
        for (var repuesto : afectados) {
            if (calculados.containsKey(repuesto.getId())) {
                repuesto.setStock(calculados.get(repuesto.getId()));
            }
        }
        repositorio.saveAll(afectados);
        if (registro.isPresent()) registro.get().actualizarFirma(objetivo);
        else descuentoStockRepositorio.save(new DescuentoStockOrden(solicitud.ordenId(), objetivo));
    }

    private Map<Long, Integer> cantidades(List<RepuestoStockRequest> repuestos) {
        Map<Long, Integer> valores = new TreeMap<>();
        for (var repuesto : repuestos) {
            if (repuesto == null || repuesto.repuestoId() == null || repuesto.cantidad() == null
                    || repuesto.repuestoId() <= 0 || repuesto.cantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repuesto o cantidad inválidos");
            }
            if (valores.putIfAbsent(repuesto.repuestoId(), repuesto.cantidad()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repuesto duplicado");
            }
        }
        return valores;
    }

    private String firma(Map<Long, Integer> cantidades) {
        if (cantidades.isEmpty()) return "-"; // Oracle transforma cadenas vacías en NULL.
        var firma = cantidades.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining("|"));
        if (firma.length() > 2000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Demasiados repuestos para la orden");
        return firma;
    }

    @Transactional
    public void descontarStock(DescontarStockRequest solicitud) {

        Map<Long, Integer> cantidadesSolicitadas = solicitud.repuestos()
                .stream()
                .collect(Collectors.toMap(
                        RepuestoStockRequest::repuestoId,
                        RepuestoStockRequest::cantidad,
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
