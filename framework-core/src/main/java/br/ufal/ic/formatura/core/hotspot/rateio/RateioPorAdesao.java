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
 * apenas os formandos que ADERIRAM ao pacote pagam, dividindo entre si.
 * Quem não aderiu paga zero.
 */
public class RateioPorAdesao implements EstrategiaRateio {

    @Override
    public Map<Formando, BigDecimal> calcular(Turma turma, BigDecimal custoTotal) {
        Map<Formando, BigDecimal> resultado = new LinkedHashMap<>();
        List<Formando> aderentes = turma.getAderentes();
        BigDecimal cota = aderentes.isEmpty() ? BigDecimal.ZERO
                : custoTotal.divide(BigDecimal.valueOf(aderentes.size()), 2, RoundingMode.HALF_UP);
        for (Formando f : turma.getFormandos()) {
            resultado.put(f, f.isAderiu() ? cota : BigDecimal.ZERO);
        }
        return resultado;
    }

    @Override
    public String nome() {
        return "Rateio por Adesão";
    }
}
