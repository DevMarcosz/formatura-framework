package br.ufal.ic.formatura.core.hotspot.pagamento;

import br.ufal.ic.formatura.core.domain.Formando;
import br.ufal.ic.formatura.core.domain.ReciboPagamento;

import java.math.BigDecimal;

/**
 * HOT SPOT 2 — Meio de Pagamento.
 *
 * <p>Implementado com o padrão <b>Template Method</b>. O método {@link #processar}
 * é a parte INVARIANTE (frozen spot, {@code final}): valida, dispara a cobrança e
 * gera o recibo, SEMPRE nessa ordem. O passo {@link #executarCobranca} é o ponto
 * variante (hot spot) que cada meio de pagamento concreto implementa.</p>
 *
 * <p>Inversão de controle: é o framework (este método template) que chama o
 * código da aplicação ({@code executarCobranca}), e não o contrário.</p>
 */
public abstract class MeioPagamento {

    /**
     * Método template (FROZEN SPOT) — não pode ser sobrescrito.
     */
    public final ReciboPagamento processar(Formando formando, BigDecimal valor) {
        validar(formando, valor);
        String comprovante = executarCobranca(formando, valor); // hook abstrato
        registrar(formando, valor, comprovante);
        return new ReciboPagamento(formando.getId(), valor, nome(), comprovante);
    }

    /** Hook com comportamento DEFAULT — pode ser estendido pelas subclasses. */
    protected void validar(Formando formando, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Valor de pagamento inválido: " + valor);
        }
    }

    /** Hook com comportamento default (log simples). */
    protected void registrar(Formando formando, BigDecimal valor, String comprovante) {
        System.out.printf("[%s] Pagamento de R$ %s por %s -> %s%n",
                nome(), valor, formando.getNome(), comprovante);
    }

    /** HOT SPOT — passo variante obrigatório de cada meio de pagamento. */
    protected abstract String executarCobranca(Formando formando, BigDecimal valor);

    /** Nome do meio de pagamento. */
    public abstract String nome();
}
