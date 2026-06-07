package br.ufal.ic.formatura.core.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cronograma/plano de um evento da formatura, produzido pela parte invariante
 * do hot spot {@code Evento}.
 */
public class Cronograma {

    private final String evento;
    private final BigDecimal custoEstimado;
    private final int participantes;
    private final List<String> etapas = new ArrayList<>();

    public Cronograma(String evento, BigDecimal custoEstimado, int participantes) {
        this.evento = evento;
        this.custoEstimado = custoEstimado;
        this.participantes = participantes;
    }

    public Cronograma addEtapa(String etapa) {
        etapas.add(etapa);
        return this;
    }

    public String getEvento() { return evento; }
    public BigDecimal getCustoEstimado() { return custoEstimado; }
    public int getParticipantes() { return participantes; }
    public List<String> getEtapas() { return etapas; }

    @Override
    public String toString() {
        return "Cronograma[" + evento + ", custo=R$ " + custoEstimado
                + ", participantes=" + participantes + ", etapas=" + etapas + "]";
    }
}
