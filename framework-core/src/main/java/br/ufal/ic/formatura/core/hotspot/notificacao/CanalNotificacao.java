package br.ufal.ic.formatura.core.hotspot.notificacao;

import br.ufal.ic.formatura.core.domain.Formando;

/**
 * HOT SPOT 5 — Canal de Notificação dos formandos.
 *
 * <p>Padrão <b>Template Method</b>. {@link #notificar} é a parte invariante:
 * verifica disponibilidade, formata a mensagem e envia. {@link #enviar} e
 * {@link #disponivel} são os pontos variantes preenchidos por cada canal.</p>
 */
public abstract class CanalNotificacao {

    /** Método template (FROZEN SPOT). */
    public final boolean notificar(Formando formando, String mensagem) {
        if (!disponivel(formando)) {
            return false;
        }
        enviar(formando, formatar(formando, mensagem));
        return true;
    }

    /** Hook default — formatação comum; subclasses podem refinar. */
    protected String formatar(Formando formando, String mensagem) {
        return "Olá " + formando.getNome() + ", " + mensagem;
    }

    /** HOT SPOT — verifica se o formando pode receber por este canal. */
    protected abstract boolean disponivel(Formando formando);

    /** HOT SPOT — envio efetivo pela tecnologia do canal. */
    protected abstract void enviar(Formando formando, String conteudo);

    public abstract String nome();
}
