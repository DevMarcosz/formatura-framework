package br.ufal.ic.formatura.core.hotspot.votacao;

import br.ufal.ic.formatura.core.domain.ResultadoVotacao;
import br.ufal.ic.formatura.core.domain.Voto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regra concreta: exige quórum mínimo de participação E maioria qualificada
 * (percentual configurável) para aprovar a opção vencedora.
 */
public class QuorumQualificado implements RegraVotacao {

    private final double quorumMinimo;     // ex.: 0.5 = 50% dos aptos devem votar
    private final double maioriaMinima;    // ex.: 0.6 = vencedora precisa de 60% dos votos

    public QuorumQualificado(double quorumMinimo, double maioriaMinima) {
        this.quorumMinimo = quorumMinimo;
        this.maioriaMinima = maioriaMinima;
    }

    @Override
    public ResultadoVotacao apurar(List<Voto> votos, int totalAptos) {
        Map<String, Integer> apuracao = new LinkedHashMap<>();
        for (Voto v : votos) {
            apuracao.merge(v.getOpcao(), 1, Integer::sum);
        }

        String vencedora = null;
        int maior = 0;
        for (Map.Entry<String, Integer> e : apuracao.entrySet()) {
            if (e.getValue() > maior) {
                maior = e.getValue();
                vencedora = e.getKey();
            }
        }

        double participacao = totalAptos == 0 ? 0 : (double) votos.size() / totalAptos;
        double percentualVencedora = votos.isEmpty() ? 0 : (double) maior / votos.size();
        boolean atingiuQuorum = participacao >= quorumMinimo;
        boolean atingiuMaioria = percentualVencedora >= maioriaMinima;
        boolean aprovada = atingiuQuorum && atingiuMaioria;

        String obs = String.format(
                "Participação %.0f%% (mín %.0f%%), vencedora %.0f%% (mín %.0f%%)",
                participacao * 100, quorumMinimo * 100,
                percentualVencedora * 100, maioriaMinima * 100);

        return new ResultadoVotacao(aprovada ? vencedora : null, aprovada, apuracao, obs);
    }

    @Override
    public String nome() {
        return "Quórum Qualificado";
    }
}
