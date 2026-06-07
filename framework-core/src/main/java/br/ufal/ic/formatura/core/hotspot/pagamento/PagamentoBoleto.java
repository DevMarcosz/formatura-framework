package br.ufal.ic.formatura.core.hotspot.pagamento;

import br.ufal.ic.formatura.core.domain.Formando;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementação concreta do hot spot {@link MeioPagamento}: cobrança via boleto.
 * Sobrescreve {@link #validar} para impor um valor mínimo de emissão.
 */
public class PagamentoBoleto extends MeioPagamento {

    private static final BigDecimal VALOR_MINIMO = new BigDecimal("5.00");

    @Override
    protected void validar(Formando formando, BigDecimal valor) {
        super.validar(formando, valor); // reaproveita a validação invariante
        if (valor.compareTo(VALOR_MINIMO) < 0) {
            throw new IllegalArgumentException("Boleto exige valor mínimo de R$ " + VALOR_MINIMO);
        }
    }

    @Override
    protected String executarCobranca(Formando formando, BigDecimal valor) {
        long numero = ThreadLocalRandom.current().nextLong(10000000000L, 99999999999L);
        return "BOLETO-" + numero;
    }

    @Override
    public String nome() {
        return "Boleto Bancário";
    }
}
