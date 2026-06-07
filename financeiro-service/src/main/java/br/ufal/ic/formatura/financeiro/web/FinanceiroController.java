package br.ufal.ic.formatura.financeiro.web;

import br.ufal.ic.formatura.core.domain.Formando;
import br.ufal.ic.formatura.core.domain.ReciboPagamento;
import br.ufal.ic.formatura.core.domain.Turma;
import br.ufal.ic.formatura.core.hotspot.pacote.FabricaPacote;
import br.ufal.ic.formatura.core.hotspot.pacote.Pacote;
import br.ufal.ic.formatura.core.hotspot.pagamento.*;
import br.ufal.ic.formatura.core.hotspot.rateio.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Expõe os hot spots Financeiros (Rateio, Pagamento, Pacote) via REST.
 * A escolha da implementação concreta vem no corpo da requisição — é a
 * "parte variante" que a aplicação cliente decide.
 */
@RestController
@RequestMapping("/financeiro")
public class FinanceiroController {

    private final FabricaPacote fabricaPacote = new FabricaPacote();

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "financeiro-service");
    }

    /** Hot spot 6: lista os pacotes disponíveis. */
    @GetMapping("/pacotes")
    public List<Map<String, Object>> pacotes() {
        return fabricaPacote.disponiveis().stream()
                .map(p -> Map.<String, Object>of(
                        "codigo", p.codigo(),
                        "nome", p.nome(),
                        "preco", p.preco(),
                        "itens", p.itens()))
                .toList();
    }

    /** Hot spot 1: calcula o rateio do custo conforme a estratégia escolhida. */
    @PostMapping("/rateio")
    public List<Map<String, Object>> rateio(@RequestBody RateioRequest req) {
        Turma turma = new Turma("turma", "curso", 2026);
        for (FormandoDTO dto : req.formandos()) {
            Formando f = new Formando(dto.id(), dto.nome(), "curso", dto.email(), dto.telefone());
            f.setAderiu(dto.aderiu());
            f.setRendaDeclarada(dto.renda());
            turma.adicionar(f);
        }
        EstrategiaRateio estrategia = estrategiaPorNome(req.estrategia());
        Map<Formando, BigDecimal> cotas = estrategia.calcular(turma, req.custoTotal());

        return cotas.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "formando", e.getKey().getNome(),
                        "valor", e.getValue()))
                .toList();
    }

    /** Hot spot 2: processa um pagamento pelo meio escolhido (Template Method). */
    @PostMapping("/pagamento")
    public Map<String, Object> pagar(@RequestBody PagamentoRequest req) {
        Formando f = new Formando(req.formandoId(), req.nome(), "curso", "x@x", "0");
        MeioPagamento meio = meioPorNome(req.meio(), req.parcelas());
        ReciboPagamento recibo = meio.processar(f, req.valor());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("formandoId", recibo.getFormandoId());
        resp.put("valor", recibo.getValor());
        resp.put("meio", recibo.getMeio());
        resp.put("comprovante", recibo.getComprovante());
        resp.put("quando", recibo.getQuando().toString());
        return resp;
    }

    // ---- Seleção das implementações de hot spot --------------------------

    private EstrategiaRateio estrategiaPorNome(String nome) {
        return switch (nome == null ? "" : nome.toUpperCase()) {
            case "ADESAO" -> new RateioPorAdesao();
            case "RENDA" -> new RateioProporcionalRenda();
            default -> new RateioIgualitario();
        };
    }

    private MeioPagamento meioPorNome(String nome, int parcelas) {
        return switch (nome == null ? "" : nome.toUpperCase()) {
            case "BOLETO" -> new PagamentoBoleto();
            case "CARTAO" -> new PagamentoCartao(parcelas <= 0 ? 1 : parcelas);
            default -> new PagamentoPix();
        };
    }

    // ---- DTOs da API -----------------------------------------------------

    public record FormandoDTO(String id, String nome, String email, String telefone,
                              boolean aderiu, double renda) {}

    public record RateioRequest(BigDecimal custoTotal, String estrategia,
                                List<FormandoDTO> formandos) {}

    public record PagamentoRequest(String formandoId, String nome, BigDecimal valor,
                                   String meio, int parcelas) {}
}
