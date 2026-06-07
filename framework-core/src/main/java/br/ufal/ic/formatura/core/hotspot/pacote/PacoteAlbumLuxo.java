package br.ufal.ic.formatura.core.hotspot.pacote;

import java.math.BigDecimal;
import java.util.List;

/** Pacote concreto: álbum impresso premium. */
public class PacoteAlbumLuxo extends Pacote {

    @Override public String codigo() { return "ALBUM"; }

    @Override public String nome() { return "Pacote Álbum de Luxo"; }

    @Override public BigDecimal preco() { return new BigDecimal("350.00"); }

    @Override
    public List<String> itens() {
        return List.of("Álbum capa dura 30x30", "30 páginas", "Caixa personalizada");
    }
}
