package br.ufal.ic.formatura.core.hotspot.notificacao;

import br.ufal.ic.formatura.core.domain.Formando;

/**
 * Canal concreto: notificação por WhatsApp.
 */
public class CanalWhatsApp extends CanalNotificacao {

    @Override
    protected boolean disponivel(Formando formando) {
        return formando.getTelefone() != null && formando.getTelefone().length() >= 10;
    }

    @Override
    protected void enviar(Formando formando, String conteudo) {
        // Integração real: WhatsApp Business API.
        System.out.println("[WhatsApp " + formando.getTelefone() + "] " + conteudo + " 📲");
    }

    @Override
    public String nome() {
        return "WhatsApp";
    }
}
