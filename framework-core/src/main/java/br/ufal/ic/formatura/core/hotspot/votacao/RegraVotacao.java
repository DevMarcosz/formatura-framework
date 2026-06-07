package br.ufal.ic.formatura.core.hotspot.votacao;

import br.ufal.ic.formatura.core.domain.ResultadoVotacao;
import br.ufal.ic.formatura.core.domain.Voto;

import java.util.List;

/**
 * HOT SPOT 4 — Regra de apuração de votações da comissão de formatura.
 *
 * <p>Padrão <b>Strategy</b>. Cada formatura decide COMO suas deliberações são
 * apuradas (maioria simples, quórum mínimo, voto ponderado por cargo...).</p>
 */
public interface RegraVotacao {

    /**
     * Apura uma votação.
     *
     * @param votos       votos coletados
     * @param totalAptos  total de eleitores aptos (para cálculo de quórum)
     * @return resultado da apuração
     */
    ResultadoVotacao apurar(List<Voto> votos, int totalAptos);

    String nome();
}
