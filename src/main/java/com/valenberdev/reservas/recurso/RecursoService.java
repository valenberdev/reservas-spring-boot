package com.valenberdev.reservas.recurso;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecursoService {

    private final RecursoRepository repository;

    public RecursoService(RecursoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RecursoResponse> listar() {
        return repository.findAll().stream().map(RecursoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public RecursoResponse obtener(Long id) {
        return RecursoResponse.desde(buscar(id));
    }

    @Transactional
    public RecursoResponse crear(RecursoRequest request) {
        Recurso recurso = new Recurso(request.nombre(), request.descripcion());
        return RecursoResponse.desde(repository.save(recurso));
    }

    @Transactional
    public RecursoResponse actualizar(Long id, RecursoRequest request) {
        Recurso recurso = buscar(id);
        recurso.setNombre(request.nombre());
        recurso.setDescripcion(request.descripcion());
        return RecursoResponse.desde(recurso);
    }

    @Transactional
    public void desactivar(Long id) {
        buscar(id).setActivo(false);
    }

    private Recurso buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado"));
    }
}