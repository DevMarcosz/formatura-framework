package br.ufal.ic.formatura.core.demo;

import br.ufal.ic.formatura.core.GestorFormatura;
import br.ufal.ic.formatura.core.domain.*;
import br.ufal.ic.formatura.core.hotspot.evento.BaileFormatura;
import br.ufal.ic.formatura.core.hotspot.evento.ColacaoGrau;
import br.ufal.ic.formatura.core.hotspot.notificacao.CanalEmail;
import br.ufal.ic.formatura.core.hotspot.notificacao.CanalWhatsApp;
import br.ufal.ic.formatura.core.hotspot.pagamento.PagamentoBoleto;
import br.ufal.ic.formatura.core.hotspot.pagamento.PagamentoPix;
import br.ufal.ic.formatura.core.hotspot.rateio.RateioIgualitario;
import br.ufal.ic.formatura.core.hotspot.rateio.RateioPorAdesao;
import br.ufal.ic.formatura.core.hotspot.votacao.MaioriaSimples;
import br.ufal.ic.formatura.core.hotspot.votacao.QuorumQualificado;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Demonstração do framework: DUAS aplicações diferentes construídas a partir do
 * MESMO núcleo, apenas "plugando" implementações distintas dos hot spots.
 *
 * <p>Equivale ao exemplo {@code Aplicacao1}/{@code Aplicacao2} + {@code Rodar}
 * apresentado em aula: a parte invariante é reutilizada; a parte variante muda.</p>
 *
 * <p>Execute com: {@code java -cp target/classes
 * br.ufal.ic.formatura.core.demo.DemoFramework}</p>
 */
public class DemoFramework {

    public static void main(String[] args) {
        System.out.println("=== APLICAÇÃO 1: Formatura de Medicina ===");
        formaturaMedicina();

        System.out.println("\n=== APLICAÇÃO 2: Formatura de Engenharia ===");
        formaturaEngenharia();
    }

    /** Aplicação 1: rateio igualitário, quórum qualificado, Pix, e-mail. */
    private static void formaturaMedicina() {
        Turma turma = new Turma("MED-2026", "Medicina", 2026);
        turma.adicionar(new Formando("1", "Ana", "Medicina", "ana@ufal.br", "82999990001"));
        turma.adicionar(new Formando("2", "Bruno", "Medicina", "bruno@ufal.br", "82999990002"));
        turma.adicionar(new Formando("3", "Carla", "Medicina", "carla@ufal.br", "82999990003"));

        GestorFormatura gestor = new GestorFormatura(turma)
                .comRateio(new RateioIgualitario())
                .comRegraVotacao(new QuorumQualificado(0.5, 0.6))
                .comCanal(new CanalEmail());

        Map<Formando, BigDecimal> cotas = gestor.ratear(new BigDecimal("30000.00"));
        cotas.forEach((f, v) -> System.out.println("  " + f + " paga R$ " + v));

        System.out.println("  " + gestor.organizarEvento(new ColacaoGrau()));

        ReciboPagamento recibo = gestor.receberPagamento(
                turma.getFormandos().get(0), new BigDecimal("10000.00"), new PagamentoPix());
        System.out.println("  " + recibo);

        ResultadoVotacao r = gestor.deliberar(List.of(
                new Voto("1", "Buffet A"), new Voto("2", "Buffet A"), new Voto("3", "Buffet B")));
        System.out.println("  Votação: " + r);

        System.out.println("  Notificações enviadas: " + gestor.comunicar("a colação foi agendada!"));
    }

    /** Aplicação 2: rateio por adesão, maioria simples, boleto, WhatsApp. */
    private static void formaturaEngenharia() {
        Turma turma = new Turma("ENG-2026", "Engenharia", 2026);
        Formando d = new Formando("4", "Diego", "Engenharia", "diego@ufal.br", "82988880004");
        Formando e = new Formando("5", "Elena", "Engenharia", "elena@ufal.br", "82988880005");
        e.setAderiu(false); // não aderiu ao pacote
        turma.adicionar(d);
        turma.adicionar(e);

        GestorFormatura gestor = new GestorFormatura(turma)
                .comRateio(new RateioPorAdesao())
                .comRegraVotacao(new MaioriaSimples())
                .comCanal(new CanalWhatsApp());

        Map<Formando, BigDecimal> cotas = gestor.ratear(new BigDecimal("20000.00"));
        cotas.forEach((f, v) -> System.out.println("  " + f + " paga R$ " + v));

        System.out.println("  " + gestor.organizarEvento(new BaileFormatura()));

        ReciboPagamento recibo = gestor.receberPagamento(
                d, new BigDecimal("20000.00"), new PagamentoBoleto());
        System.out.println("  " + recibo);

        System.out.println("  Notificações enviadas: " + gestor.comunicar("o baile está confirmado!"));
    }
}
