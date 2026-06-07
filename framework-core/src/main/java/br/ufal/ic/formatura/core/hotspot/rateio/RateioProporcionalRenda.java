package br.ufal.ic.formatura.core.hotspot.rateio;

import br.ufal.ic.formatura.core.domain.Formando;
import br.ufal.ic.formatura.core.domain.Turma;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação concreta do hot spot {@link EstrategiaRateio}:
 * rateio solidário — cada formando paga proporcionalmente à renda declarada.
 * Demonstra como uma nova política pode ser "plugada" sem alterar o framework.
 */
public class RateioProporcionalRenda implements EstrategiaRateio {

    @Override
    public Map<Formando, BigDecimal> calcular(Turma turma, BigDecimal custoTotal) {
        Map<Formando, BigDecimal> resultado = new LinkedHashMap<>();
        List<Formando> formandos = turma.getFormandos();

        double somaRendas = 0.0;
        for (Formando f : formandos) {
            somaRendas += Math.max(f.getRendaDeclarada(), 0.0);
        }
        // Sem rendas informadas, recai no comportamento igualitário.
        if (somaRendas <= 0.0) {
            return new RateioIgualitario().calcular(turma, custoTotal);
        }

        for (Formando f : formandos) {
            double fracao = Math.max(f.getRendaDeclarada(), 0.0) / somaRendas;
            BigDecimal valor = custoTotal.multiply(BigDecimal.valueOf(fracao))
                    .setScale(2, RoundingMode.HALF_UP);
            resultado.put(f, valor);
        }
        return resultado;
    }

    @Override
    public String nome() {
        return "Rateio Proporcional à Renda";
    }
}
