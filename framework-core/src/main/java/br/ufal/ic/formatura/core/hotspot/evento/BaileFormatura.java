package br.ufal.ic.formatura.core.hotspot.evento;

import br.ufal.ic.formatura.core.domain.Cronograma;
import br.ufal.ic.formatura.core.domain.Turma;

import java.math.BigDecimal;

/**
 * Evento concreto: Baile de Formatura (festa, apenas aderentes).
 */
public class BaileFormatura extends Evento {

    private static final BigDecimal CUSTO_FIXO_ESPACO = new BigDecimal("8000.00");
    private static final BigDecimal CUSTO_POR_CONVIDADO = new BigDecimal("90.00");

    public BaileFormatura() {
        super("Baile de Formatura");
    }

    @Override
    protected void validarParticipantes(Turma turma) {
        if (turma.getAderentes().isEmpty()) {
            throw new IllegalStateException("Baile exige ao menos 1 formando aderente.");
        }
    }

    @Override
    protected int contarParticipantes(Turma turma) {
        // Baile considera apenas aderentes (cada um com 1 acompanhante).
        return turma.getAderentes().size() * 2;
    }

    @Override
    protected BigDecimal calcularCusto(Turma turma) {
        int convidados = contarParticipantes(turma);
        return CUSTO_FIXO_ESPACO.add(
                CUSTO_POR_CONVIDADO.multiply(BigDecimal.valueOf(convidados)));
    }

    @Override
    protected void montarProgramacao(Cronograma cronograma, Turma turma) {
        cronograma.addEtapa("Recepção e coquetel");
        cronograma.addEtapa("Jantar");
        cronograma.addEtapa("Show / banda ao vivo");
        cronograma.addEtapa("Pista de dança");
    }
}
