package br.ufal.ic.formatura.core.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado da operação de pagamento (parte invariante).
 */
public class ReciboPagamento {

    private final String formandoId;
    private final BigDecimal valor;
    private final String meio;
    private final String comprovante;
    private final LocalDateTime quando;

    public ReciboPagamento(String formandoId, BigDecimal valor, String meio, String comprovante) {
        this.formandoId = formandoId;
        this.valor = valor;
        this.meio = meio;
        this.comprovante = comprovante;
        this.quando = LocalDateTime.now();
    }

    public String getFormandoId() { return formandoId; }
    public BigDecimal getValor() { return valor; }
    public String getMeio() { return meio; }
    public String getComprovante() { return comprovante; }
    public LocalDateTime getQuando() { return quando; }

    @Override
    public String toString() {
        return "Recibo[" + meio + " R$ " + valor + " -> " + comprovante + "]";
    }
}
