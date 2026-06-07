package br.ufal.ic.formatura.core.hotspot.evento;

import br.ufal.ic.formatura.core.domain.Cronograma;
import br.ufal.ic.formatura.core.domain.Turma;

import java.math.BigDecimal;

/**
 * HOT SPOT 3 — Evento da formatura (colação de grau, baile, missa, jantar...).
 *
 * <p>Implementado com o padrão <b>Template Method</b>. O método {@link #organizar}
 * define o esqueleto INVARIANTE de organização de qualquer evento; os passos
 * variantes ({@link #calcularCusto} e {@link #validarParticipantes}) são hot spots
 * preenchidos por cada tipo de evento concreto. {@link #reservarLocal} é um hook
 * com comportamento default, opcionalmente sobrescrito.</p>
 */
public abstract class Evento {

    protected final String nome;

    protected Evento(String nome) {
        this.nome = nome;
    }

    /** Método template (FROZEN SPOT). */
    public final Cronograma organizar(Turma turma) {
        validarParticipantes(turma);                 // hot spot
        BigDecimal custo = calcularCusto(turma);      // hot spot
        Cronograma cronograma = new Cronograma(nome, custo, contarParticipantes(turma));
        cronograma.addEtapa(reservarLocal());         // hook default
        montarProgramacao(cronograma, turma);         // hot spot
        cronograma.addEtapa("Encerramento de " + nome);
        return cronograma;
    }

    /** Hook com comportamento default — pode ser sobrescrito. */
    protected String reservarLocal() {
        return "Reserva de local padrão para " + nome;
    }

    /** Por padrão, todos os formandos participam; subclasses podem refinar. */
    protected int contarParticipantes(Turma turma) {
        return turma.tamanho();
    }

    /** HOT SPOT — custo do evento. */
    protected abstract BigDecimal calcularCusto(Turma turma);

    /** HOT SPOT — valida quem pode participar (lança exceção se inválido). */
    protected abstract void validarParticipantes(Turma turma);

    /** HOT SPOT — etapas específicas da programação do evento. */
    protected abstract void montarProgramacao(Cronograma cronograma, Turma turma);

    public String getNome() {
        return nome;
    }
}
