package br.ufal.ic.formatura.core.hotspot.evento;

import br.ufal.ic.formatura.core.domain.Cronograma;
import br.ufal.ic.formatura.core.domain.Turma;

import java.math.BigDecimal;

/**
 * Evento concreto: Colação de Grau (cerimônia oficial, obrigatória).
 */
public class ColacaoGrau extends Evento {

    private static final BigDecimal CUSTO_POR_FORMANDO = new BigDecimal("120.00");

    public ColacaoGrau() {
        super("Colação de Grau");
    }

    @Override
    protected void validarParticipantes(Turma turma) {
        if (turma.tamanho() == 0) {
            throw new IllegalStateException("Colação de grau exige ao menos 1 formando.");
        }
    }

    @Override
    protected BigDecimal calcularCusto(Turma turma) {
        return CUSTO_POR_FORMANDO.multiply(BigDecimal.valueOf(turma.tamanho()));
    }

    @Override
    protected String reservarLocal() {
        return "Reserva do auditório/teatro oficial da instituição";
    }

    @Override
    protected void montarProgramacao(Cronograma cronograma, Turma turma) {
        cronograma.addEtapa("Entrada solene dos formandos");
        cronograma.addEtapa("Juramento da turma");
        cronograma.addEtapa("Entrega de diplomas");
        cronograma.addEtapa("Discurso do paraninfo");
    }
}
