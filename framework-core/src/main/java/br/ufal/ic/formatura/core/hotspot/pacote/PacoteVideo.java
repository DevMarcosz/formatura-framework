package br.ufal.ic.formatura.core.hotspot.pacote;

import java.math.BigDecimal;
import java.util.List;

/** Pacote concreto: filmagem/vídeo. */
public class PacoteVideo extends Pacote {

    @Override public String codigo() { return "VIDEO"; }

    @Override public String nome() { return "Pacote Vídeo"; }

    @Override public BigDecimal preco() { return new BigDecimal("600.00"); }

    @Override
    public List<String> itens() {
        return List.of("Filmagem em 4K", "Edição com trilha", "Aftermovie da turma");
    }
}
