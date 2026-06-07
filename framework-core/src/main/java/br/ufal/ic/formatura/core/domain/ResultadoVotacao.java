package br.ufal.ic.formatura.core.domain;

import java.util.Map;

/**
 * Resultado da apuração de uma votação (parte invariante).
 */
public class ResultadoVotacao {

    private final String opcaoVencedora;
    private final boolean aprovada;
    private final Map<String, Integer> apuracao;
    private final String observacao;

    public ResultadoVotacao(String opcaoVencedora, boolean aprovada,
                            Map<String, Integer> apuracao, String observacao) {
        this.opcaoVencedora = opcaoVencedora;
        this.aprovada = aprovada;
        this.apuracao = apuracao;
        this.observacao = observacao;
    }

    public String getOpcaoVencedora() { return opcaoVencedora; }
    public boolean isAprovada() { return aprovada; }
    public Map<String, Integer> getApuracao() { return apuracao; }
    public String getObservacao() { return observacao; }

    @Override
    public String toString() {
        return "Resultado[vencedora=" + opcaoVencedora + ", aprovada=" + aprovada
                + ", " + observacao + "]";
    }
}
