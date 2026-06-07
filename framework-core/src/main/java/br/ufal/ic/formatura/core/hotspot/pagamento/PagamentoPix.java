package br.ufal.ic.formatura.core.hotspot.pagamento;

import br.ufal.ic.formatura.core.domain.Formando;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementação concreta do hot spot {@link MeioPagamento}: cobrança via Pix.
 * Gera um identificador de transação (txid) simulando a API de um PSP.
 */
public class PagamentoPix extends MeioPagamento {

    @Override
    protected String executarCobranca(Formando formando, BigDecimal valor) {
        String txid = "PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        // Aqui entraria a integração real com o provedor (ex.: gerar QR Code).
        return txid;
    }

    @Override
    public String nome() {
        return "Pix";
    }
}
