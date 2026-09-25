package cl.digitalfix.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.digitalfix.catalog.entity.Repuesto;
import cl.digitalfix.catalog.exception.RecursoNoEncontradoException;
import cl.digitalfix.catalog.repository.RepuestoRepositorio;

@Service
public class RepuestoServicio {

    private final RepuestoRepositorio repositorio;

    public RepuestoServicio(RepuestoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Repuesto> listarRepuestos() {
        return repositorio.findAll();
    }

    public Repuesto crearRepuesto(Repuesto repuesto) {
        repuesto.setId(null);
        return repositorio.save(repuesto);
    }

    public Repuesto actualizarRepuesto(Long id, Repuesto datosActualizados) {
        Repuesto repuesto = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un repuesto con id " + id));

        repuesto.setNombre(datosActualizados.getNombre());
        repuesto.setDescripcion(datosActualizados.getDescripcion());
        repuesto.setStock(datosActualizados.getStock());

        return repositorio.save(repuesto);
    }

    public void eliminarRepuesto(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "No existe un repuesto con id " + id);
        }

        repositorio.deleteById(id);
    }
}