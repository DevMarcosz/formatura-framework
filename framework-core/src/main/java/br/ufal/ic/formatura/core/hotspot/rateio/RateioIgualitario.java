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
 * divide o custo igualmente entre TODOS os formandos da turma.
 */
public class RateioIgualitario implements EstrategiaRateio {

    @Override
    public Map<Formando, BigDecimal> calcular(Turma turma, BigDecimal custoTotal) {
        Map<Formando, BigDecimal> resultado = new LinkedHashMap<>();
        List<Formando> formandos = turma.getFormandos();
        if (formandos.isEmpty()) {
            return resultado;
        }
        BigDecimal cota = custoTotal.divide(
                BigDecimal.valueOf(formandos.size()), 2, RoundingMode.HALF_UP);
        for (Formando f : formandos) {
            resultado.put(f, cota);
        }
        return resultado;
    }

    @Override
    public String nome() {
        return "Rateio Igualitário";
    }
}
