package br.ufal.ic.formatura.core.hotspot.notificacao;

import br.ufal.ic.formatura.core.domain.Formando;

/**
 * Canal concreto: notificação por SMS (limite de 160 caracteres).
 */
public class CanalSMS extends CanalNotificacao {

    @Override
    protected boolean disponivel(Formando formando) {
        return formando.getTelefone() != null && !formando.getTelefone().isBlank();
    }

    @Override
    protected String formatar(Formando formando, String mensagem) {
        String texto = super.formatar(formando, mensagem);
        return texto.length() > 160 ? texto.substring(0, 157) + "..." : texto;
    }

    @Override
    protected void enviar(Formando formando, String conteudo) {
        // Integração real: gateway de SMS.
        System.out.println("[SMS " + formando.getTelefone() + "] " + conteudo);
    }

    @Override
    public String nome() {
        return "SMS";
    }
}
