package br.ufal.ic.formatura.core.hotspot.votacao;

import br.ufal.ic.formatura.core.domain.ResultadoVotacao;
import br.ufal.ic.formatura.core.domain.Voto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regra concreta: vence a opção mais votada (maioria simples).
 */
public class MaioriaSimples implements RegraVotacao {

    @Override
    public ResultadoVotacao apurar(List<Voto> votos, int totalAptos) {
        Map<String, Integer> apuracao = new LinkedHashMap<>();
        for (Voto v : votos) {
            apuracao.merge(v.getOpcao(), 1, Integer::sum);
        }
        String vencedora = null;
        int maior = -1;
        for (Map.Entry<String, Integer> e : apuracao.entrySet()) {
            if (e.getValue() > maior) {
                maior = e.getValue();
                vencedora = e.getKey();
            }
        }
        boolean aprovada = vencedora != null;
        return new ResultadoVotacao(vencedora, aprovada, apuracao,
                "Maioria simples: " + maior + " voto(s)");
    }

    @Override
    public String nome() {
        return "Maioria Simples";
    }
}
