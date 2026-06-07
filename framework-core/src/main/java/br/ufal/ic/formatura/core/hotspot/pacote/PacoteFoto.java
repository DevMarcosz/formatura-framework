package br.ufal.ic.formatura.core.hotspot.pacote;

import java.math.BigDecimal;
import java.util.List;

/** Pacote concreto: cobertura fotográfica. */
public class PacoteFoto extends Pacote {

    @Override public String codigo() { return "FOTO"; }

    @Override public String nome() { return "Pacote Fotografia"; }

    @Override public BigDecimal preco() { return new BigDecimal("450.00"); }

    @Override
    public List<String> itens() {
        return List.of("Cobertura da colação", "50 fotos digitais", "Ensaio individual");
    }
}
