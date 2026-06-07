package br.ufal.ic.formatura.comissao.web;

import br.ufal.ic.formatura.core.domain.ResultadoVotacao;
import br.ufal.ic.formatura.core.domain.Voto;
import br.ufal.ic.formatura.core.hotspot.votacao.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Expõe o hot spot {@link RegraVotacao}. A regra de apuração é escolhida pelo
 * cliente, demonstrando a substituibilidade de estratégias.
 */
@RestController
@RequestMapping("/comissao")
public class ComissaoController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "comissao-service");
    }

    @GetMapping("/regras")
    public List<String> regras() {
        return List.of("MAIORIA", "QUORUM", "PONDERADO");
    }

    @PostMapping("/apurar")
    public Map<String, Object> apurar(@RequestBody ApuracaoRequest req) {
        List<Voto> votos = req.votos().stream()
                .map(v -> new Voto(v.formandoId(), v.opcao(), v.peso() <= 0 ? 1 : v.peso()))
                .toList();
        RegraVotacao regra = regraPorNome(req.regra());
        ResultadoVotacao r = regra.apurar(votos, req.totalAptos());
        return Map.of(
                "regra", regra.nome(),
                "opcaoVencedora", r.getOpcaoVencedora() == null ? "" : r.getOpcaoVencedora(),
                "aprovada", r.isAprovada(),
                "apuracao", r.getApuracao(),
                "observacao", r.getObservacao());
    }

    private RegraVotacao regraPorNome(String nome) {
        return switch (nome == null ? "" : nome.toUpperCase()) {
            case "QUORUM" -> new QuorumQualificado(0.5, 0.6);
            case "PONDERADO" -> new VotoPonderado();
            default -> new MaioriaSimples();
        };
    }

    public record VotoDTO(String formandoId, String opcao, int peso) {}

    public record ApuracaoRequest(String regra, int totalAptos, List<VotoDTO> votos) {}
}
