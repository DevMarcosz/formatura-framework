package br.ufal.ic.formatura.core.hotspot.notificacao;

import br.ufal.ic.formatura.core.domain.Formando;

/**
 * Canal concreto: notificação por e-mail.
 */
public class CanalEmail extends CanalNotificacao {

    @Override
    protected boolean disponivel(Formando formando) {
        return formando.getEmail() != null && formando.getEmail().contains("@");
    }

    @Override
    protected String formatar(Formando formando, String mensagem) {
        return "[E-mail para " + formando.getEmail() + "] "
                + super.formatar(formando, mensagem);
    }

    @Override
    protected void enviar(Formando formando, String conteudo) {
        // Integração real: SMTP / provedor de e-mail.
        System.out.println(conteudo);
    }

    @Override
    public String nome() {
        return "E-mail";
    }
}
