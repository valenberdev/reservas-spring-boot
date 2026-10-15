package com.valenberdev.reservas.recurso;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    private final RecursoService service;

    public RecursoController(RecursoService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecursoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RecursoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecursoResponse crear(@Valid @RequestBody RecursoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public RecursoResponse actualizar(@PathVariable Long id, @Valid @RequestBody RecursoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }
}