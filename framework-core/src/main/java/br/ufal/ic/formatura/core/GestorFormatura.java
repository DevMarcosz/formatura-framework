package br.ufal.ic.formatura.core;

import br.ufal.ic.formatura.core.domain.*;
import br.ufal.ic.formatura.core.hotspot.evento.Evento;
import br.ufal.ic.formatura.core.hotspot.notificacao.CanalNotificacao;
import br.ufal.ic.formatura.core.hotspot.pagamento.MeioPagamento;
import br.ufal.ic.formatura.core.hotspot.rateio.EstrategiaRateio;
import br.ufal.ic.formatura.core.hotspot.votacao.RegraVotacao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PARTE INVARIANTE do framework (frozen spots) — o "main program reutilizável".
 *
 * <p>O {@code GestorFormatura} conhece o fluxo de um sistema de formatura e
 * COORDENA os pontos de extensão. As decisões variantes são injetadas por
 * composição (framework caixa-preta): a aplicação escolhe quais implementações
 * de cada hot spot serão "plugadas". É o gestor quem chama o código da aplicação
 * (inversão de controle), e não o contrário.</p>
 */
public class GestorFormatura {

    private final Turma turma;
    private EstrategiaRateio estrategiaRateio;
    private RegraVotacao regraVotacao;
    private final List<CanalNotificacao> canais = new ArrayList<>();

    public GestorFormatura(Turma turma) {
        this.turma = turma;
    }

    // ---- Configuração dos hot spots (injeção por composição) -------------

    public GestorFormatura comRateio(EstrategiaRateio estrategia) {
        this.estrategiaRateio = estrategia;
        return this;
    }

    public GestorFormatura comRegraVotacao(RegraVotacao regra) {
        this.regraVotacao = regra;
        return this;
    }

    public GestorFormatura comCanal(CanalNotificacao canal) {
        this.canais.add(canal);
        return this;
    }

    // ---- Operações invariantes que delegam aos hot spots -----------------

    /** Distribui o custo entre os formandos usando a estratégia plugada. */
    public Map<Formando, BigDecimal> ratear(BigDecimal custoTotal) {
        exigir(estrategiaRateio != null, "Estratégia de rateio não configurada.");
        return estrategiaRateio.calcular(turma, custoTotal);
    }

    /** Recebe o pagamento de um formando pelo meio escolhido. */
    public ReciboPagamento receberPagamento(Formando formando, BigDecimal valor,
                                            MeioPagamento meio) {
        return meio.processar(formando, valor);
    }

    /** Organiza um evento usando o template method do próprio evento. */
    public Cronograma organizarEvento(Evento evento) {
        return evento.organizar(turma);
    }

    /** Apura uma deliberação da comissão com a regra plugada. */
    public ResultadoVotacao deliberar(List<Voto> votos) {
        exigir(regraVotacao != null, "Regra de votação não configurada.");
        return regraVotacao.apurar(votos, turma.tamanho());
    }

    /** Comunica todos os formandos por todos os canais configurados. */
    public int comunicar(String mensagem) {
        int enviados = 0;
        for (Formando f : turma.getFormandos()) {
            for (CanalNotificacao canal : canais) {
                if (canal.notificar(f, mensagem)) {
                    enviados++;
                }
            }
        }
        return enviados;
    }

    public Turma getTurma() {
        return turma;
    }

    private static void exigir(boolean condicao, String msg) {
        if (!condicao) {
            throw new IllegalStateException(msg);
        }
    }
}
