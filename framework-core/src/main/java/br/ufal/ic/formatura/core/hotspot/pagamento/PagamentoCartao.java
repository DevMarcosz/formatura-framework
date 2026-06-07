package br.ufal.ic.formatura.core.hotspot.pagamento;

import br.ufal.ic.formatura.core.domain.Formando;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementação concreta do hot spot {@link MeioPagamento}: cartão de crédito
 * com parcelamento. Demonstra estado/configuração próprios da subclasse.
 */
public class PagamentoCartao extends MeioPagamento {

    private final int parcelas;

    public PagamentoCartao(int parcelas) {
        this.parcelas = Math.max(parcelas, 1);
    }

    @Override
    protected String executarCobranca(Formando formando, BigDecimal valor) {
        String auth = "AUTH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return auth + " (" + parcelas + "x)";
    }

    @Override
    public String nome() {
        return "Cartão de Crédito " + parcelas + "x";
    }
}
