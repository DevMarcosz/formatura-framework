package br.ufal.ic.formatura.core.hotspot.rateio;

import br.ufal.ic.formatura.core.domain.Formando;
import br.ufal.ic.formatura.core.domain.Turma;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HOT SPOT 1 — Estratégia de Rateio do custo da formatura.
 *
 * <p>Ponto de extensão (variation point) implementado com o padrão <b>Strategy</b>.
 * Cada sistema de formatura concreto decide COMO o custo total é dividido entre
 * os formandos. O framework fornece a parte invariante (cobrança, recibos,
 * relatórios) e delega esta decisão variante à aplicação.</p>
 *
 * <p>Na notação UML-F: operação {@code << adapt-static >>}.</p>
 */
public interface EstrategiaRateio {

    /**
     * Calcula quanto cada formando deve pagar.
     *
     * @param turma      turma de formandos
     * @param custoTotal custo total da formatura
     * @return mapa formando -> valor devido
     */
    Map<Formando, BigDecimal> calcular(Turma turma, BigDecimal custoTotal);

    /** Nome legível da estratégia (para relatórios e UI). */
    String nome();
}
