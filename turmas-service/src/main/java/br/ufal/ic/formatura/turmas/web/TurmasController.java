package br.ufal.ic.formatura.turmas.web;

import br.ufal.ic.formatura.core.domain.Formando;
import br.ufal.ic.formatura.core.domain.Turma;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CRUD em memória de turmas e formandos, reutilizando as classes de domínio
 * {@link Turma} e {@link Formando} do framework-core.
 */
@RestController
@RequestMapping("/turmas")
public class TurmasController {

    private final Map<String, Turma> turmas = new ConcurrentHashMap<>();

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "turmas-service");
    }

    @PostMapping
    public Map<String, Object> criar(@RequestBody CriarTurmaRequest req) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        turmas.put(id, new Turma(id, req.curso(), req.ano()));
        return Map.of("id", id, "curso", req.curso(), "ano", req.ano());
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return turmas.values().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(), "curso", t.getCurso(),
                        "ano", t.getAno(), "formandos", t.tamanho()))
                .toList();
    }

    @PostMapping("/{id}/formandos")
    public Map<String, Object> adicionarFormando(@PathVariable String id,
                                                 @RequestBody FormandoDTO dto) {
        Turma turma = turmas.get(id);
        if (turma == null) {
            throw new NoSuchElementException("Turma não encontrada: " + id);
        }
        Formando f = new Formando(UUID.randomUUID().toString().substring(0, 8),
                dto.nome(), turma.getCurso(), dto.email(), dto.telefone());
        f.setAderiu(dto.aderiu());
        turma.adicionar(f);
        return Map.of("turma", id, "formando", f.getNome(), "total", turma.tamanho());
    }

    @GetMapping("/{id}/formandos")
    public List<Map<String, Object>> formandos(@PathVariable String id) {
        Turma turma = turmas.get(id);
        if (turma == null) {
            throw new NoSuchElementException("Turma não encontrada: " + id);
        }
        return turma.getFormandos().stream()
                .map(f -> Map.<String, Object>of(
                        "id", f.getId(), "nome", f.getNome(),
                        "email", f.getEmail(), "aderiu", f.isAderiu()))
                .toList();
    }

    public record CriarTurmaRequest(String curso, int ano) {}

    public record FormandoDTO(String nome, String email, String telefone, boolean aderiu) {}
}
