package br.ufal.ic.formatura.core.hotspot.votacao;

import br.ufal.ic.formatura.core.domain.ResultadoVotacao;
import br.ufal.ic.formatura.core.domain.Voto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regra concreta: cada voto vale conforme seu peso (ex.: diretoria da comissão
 * tem peso maior). Vence a opção com maior soma ponderada.
 */
public class VotoPonderado implements RegraVotacao {

    @Override
    public ResultadoVotacao apurar(List<Voto> votos, int totalAptos) {
        Map<String, Integer> apuracao = new LinkedHashMap<>();
        for (Voto v : votos) {
            apuracao.merge(v.getOpcao(), v.getPeso(), Integer::sum);
        }
        String vencedora = null;
        int maior = -1;
        for (Map.Entry<String, Integer> e : apuracao.entrySet()) {
            if (e.getValue() > maior) {
                maior = e.getValue();
                vencedora = e.getKey();
            }
        }
        return new ResultadoVotacao(vencedora, vencedora != null, apuracao,
                "Voto ponderado: " + maior + " ponto(s) para a vencedora");
    }

    @Override
    public String nome() {
        return "Voto Ponderado";
    }
}
