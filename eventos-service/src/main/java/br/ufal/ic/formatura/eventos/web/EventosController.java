package br.ufal.ic.formatura.eventos.web;

import br.ufal.ic.formatura.core.domain.Cronograma;
import br.ufal.ic.formatura.core.domain.Formando;
import br.ufal.ic.formatura.core.domain.Turma;
import br.ufal.ic.formatura.core.hotspot.evento.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Expõe o hot spot {@link Evento}. O cliente informa o tipo de evento; o serviço
 * usa o Template Method {@code organizar()} para montar o cronograma.
 */
@RestController
@RequestMapping("/eventos")
public class EventosController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "eventos-service");
    }

    /** Tipos de evento suportados pelo framework. */
    @GetMapping("/tipos")
    public List<String> tipos() {
        return List.of("COLACAO", "BAILE", "MISSA");
    }

    /** Organiza um evento e devolve o cronograma (custo, participantes, etapas). */
    @PostMapping("/organizar")
    public Map<String, Object> organizar(@RequestBody OrganizarRequest req) {
        Turma turma = new Turma("turma", "curso", 2026);
        for (FormandoDTO dto : req.formandos()) {
            Formando f = new Formando(dto.id(), dto.nome(), "curso", dto.email(), dto.telefone());
            f.setAderiu(dto.aderiu());
            turma.adicionar(f);
        }
        Evento evento = eventoPorTipo(req.tipo());
        Cronograma c = evento.organizar(turma);
        return Map.of(
                "evento", c.getEvento(),
                "custoEstimado", c.getCustoEstimado(),
                "participantes", c.getParticipantes(),
                "etapas", c.getEtapas());
    }

    private Evento eventoPorTipo(String tipo) {
        return switch (tipo == null ? "" : tipo.toUpperCase()) {
            case "BAILE" -> new BaileFormatura();
            case "MISSA" -> new MissaFormatura();
            default -> new ColacaoGrau();
        };
    }

    public record FormandoDTO(String id, String nome, String email, String telefone, boolean aderiu) {}

    public record OrganizarRequest(String tipo, List<FormandoDTO> formandos) {}
}
