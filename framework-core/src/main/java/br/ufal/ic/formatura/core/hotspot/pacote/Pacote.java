package br.ufal.ic.formatura.core.hotspot.pacote;

import java.math.BigDecimal;
import java.util.List;

/**
 * HOT SPOT 6 — Pacote/Produto comercializado na formatura
 * (fotos, vídeo, álbum, brindes...).
 *
 * <p>Classe abstrata que define a interface comum dos produtos. Novas
 * subclasses podem ser "plugadas" e registradas na {@link FabricaPacote}
 * sem alterar o framework (Abstract Factory / Open-Closed).</p>
 */
public abstract class Pacote {

    /** Identificador usado para seleção/registro na fábrica. */
    public abstract String codigo();

    /** Nome comercial do pacote. */
    public abstract String nome();

    /** Preço do pacote. */
    public abstract BigDecimal preco();

    /** Itens inclusos no pacote. */
    public abstract List<String> itens();

    @Override
    public String toString() {
        return nome() + " (R$ " + preco() + ")";
    }
}
