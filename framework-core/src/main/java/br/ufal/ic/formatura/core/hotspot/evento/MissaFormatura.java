package br.ufal.ic.formatura.core.hotspot.evento;

import br.ufal.ic.formatura.core.domain.Cronograma;
import br.ufal.ic.formatura.core.domain.Turma;

import java.math.BigDecimal;

/**
 * Evento concreto: Missa/Culto de Formatura (opcional, custo simbólico).
 * Demonstra um evento de baixo custo cuja programação difere bastante das demais.
 */
public class MissaFormatura extends Evento {

    private static final BigDecimal CUSTO_FIXO = new BigDecimal("1500.00");

    public MissaFormatura() {
        super("Missa de Formatura");
    }

    @Override
    protected void validarParticipantes(Turma turma) {
        // Evento aberto: não há restrição de participantes.
    }

    @Override
    protected BigDecimal calcularCusto(Turma turma) {
        return CUSTO_FIXO; // ornamentação, celebrante, coral
    }

    @Override
    protected void montarProgramacao(Cronograma cronograma, Turma turma) {
        cronograma.addEtapa("Acolhida");
        cronograma.addEtapa("Celebração");
        cronograma.addEtapa("Bênção da turma");
    }
}
