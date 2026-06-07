package br.ufal.ic.formatura.core;

import br.ufal.ic.formatura.core.domain.*;
import br.ufal.ic.formatura.core.hotspot.evento.BaileFormatura;
import br.ufal.ic.formatura.core.hotspot.evento.ColacaoGrau;
import br.ufal.ic.formatura.core.hotspot.evento.MissaFormatura;
import br.ufal.ic.formatura.core.hotspot.notificacao.CanalEmail;
import br.ufal.ic.formatura.core.hotspot.notificacao.CanalSMS;
import br.ufal.ic.formatura.core.hotspot.notificacao.CanalWhatsApp;
import br.ufal.ic.formatura.core.hotspot.pacote.FabricaPacote;
import br.ufal.ic.formatura.core.hotspot.pacote.Pacote;
import br.ufal.ic.formatura.core.hotspot.pacote.PacoteFoto;
import br.ufal.ic.formatura.core.hotspot.pagamento.PagamentoBoleto;
import br.ufal.ic.formatura.core.hotspot.pagamento.PagamentoCartao;
import br.ufal.ic.formatura.core.hotspot.pagamento.PagamentoPix;
import br.ufal.ic.formatura.core.hotspot.rateio.RateioIgualitario;
import br.ufal.ic.formatura.core.hotspot.rateio.RateioPorAdesao;
import br.ufal.ic.formatura.core.hotspot.rateio.RateioProporcionalRenda;
import br.ufal.ic.formatura.core.hotspot.votacao.MaioriaSimples;
import br.ufal.ic.formatura.core.hotspot.votacao.QuorumQualificado;
import br.ufal.ic.formatura.core.hotspot.votacao.VotoPonderado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes abrangentes dos hot spots do framework, provando a substituibilidade
 * das implementações (Liskov) e o comportamento da parte invariante.
 *
 * <p>Organizado por hot spot: cada seção testa o contrato da abstração e
 * verifica que trocar a implementação concreta muda o resultado —
 * ou seja, o ponto de extensão funciona.</p>
 */
class FrameworkTest {

    // ── Fixtures ──────────────────────────────────────────────

    private Turma turmaComTres() {
        Turma t = new Turma("T1", "Computação", 2026);
        t.adicionar(new Formando("1", "A", "Computação", "a@ufal.br", "8290001"));
        t.adicionar(new Formando("2", "B", "Computação", "b@ufal.br", "8290002"));
        Formando c = new Formando("3", "C", "Computação", "c@ufal.br", "8290003");
        c.setAderiu(false);
        t.adicionar(c);
        return t;
    }

    private Turma turmaVazia() {
        return new Turma("VAZIA", "Teste", 2026);
    }

    private Turma turmaComRendas() {
        Turma t = new Turma("R1", "Engenharia", 2026);
        Formando f1 = new Formando("1", "X", "Eng", "x@ufal.br", "8200001");
        f1.setRendaDeclarada(2000);
        Formando f2 = new Formando("2", "Y", "Eng", "y@ufal.br", "8200002");
        f2.setRendaDeclarada(8000);
        t.adicionar(f1);
        t.adicionar(f2);
        return t;
    }

    // ═══════════════════════════════════════════════════════════
    // HOT SPOT 1 — EstrategiaRateio (Strategy)
    // ═══════════════════════════════════════════════════════════

    @Test
    void rateioIgualitarioDivideEntreTodos() {
        Map<Formando, BigDecimal> cotas =
                new RateioIgualitario().calcular(turmaComTres(), new BigDecimal("300.00"));
        assertEquals(3, cotas.size());
        cotas.values().forEach(v -> assertEquals(new BigDecimal("100.00"), v));
    }

    @Test
    void rateioPorAdesaoIsentaQuemNaoAderiu() {
        Map<Formando, BigDecimal> cotas =
                new RateioPorAdesao().calcular(turmaComTres(), new BigDecimal("300.00"));
        // 2 aderentes dividem 300 = 150 cada; o terceiro paga 0.
        long pagam150 = cotas.values().stream()
                .filter(v -> v.compareTo(new BigDecimal("150.00")) == 0).count();
        long pagamZero = cotas.values().stream()
                .filter(v -> v.signum() == 0).count();
        assertEquals(2, pagam150);
        assertEquals(1, pagamZero);
    }

    @Test
    void rateioProporcionalRendaDistribuiPorProporcao() {
        // X ganha 2000, Y ganha 8000 → total rendas = 10000
        // X paga 2000/10000 * 1000 = 200, Y paga 800
        Map<Formando, BigDecimal> cotas =
                new RateioProporcionalRenda().calcular(turmaComRendas(), new BigDecimal("1000.00"));
        assertEquals(2, cotas.size());
        // Verificar que a soma bate com o total
        BigDecimal soma = cotas.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(soma.compareTo(new BigDecimal("1000.00")) == 0,
                "Soma das cotas deve ser igual ao custo total");
    }

    @Test
    void trocarEstrategiaAlteraResultado() {
        Turma turma = turmaComTres();
        BigDecimal custoTotal = new BigDecimal("300.00");

        Map<Formando, BigDecimal> igualitario = new RateioIgualitario().calcular(turma, custoTotal);
        Map<Formando, BigDecimal> porAdesao = new RateioPorAdesao().calcular(turma, custoTotal);

        // No igualitário, ninguém paga zero; no por adesão, C paga zero.
        boolean algumZeroIgualitario = igualitario.values().stream().anyMatch(v -> v.signum() == 0);
        boolean algumZeroPorAdesao = porAdesao.values().stream().anyMatch(v -> v.signum() == 0);

        assertFalse(algumZeroIgualitario, "Rateio igualitário não deve ter cota zero");
        assertTrue(algumZeroPorAdesao, "Rateio por adesão deve isentar quem não aderiu");
    }

    // ═══════════════════════════════════════════════════════════
    // HOT SPOT 2 — MeioPagamento (Template Method)
    // ═══════════════════════════════════════════════════════════

    @Test
    void pagamentoPixGeraComprovante() {
        Formando f = new Formando("1", "A", "Computação", "a@ufal.br", "8290001");
        ReciboPagamento recibo = new PagamentoPix().processar(f, new BigDecimal("100.00"));
        assertEquals("Pix", recibo.getMeio());
        assertTrue(recibo.getComprovante().startsWith("PIX-"));
    }

    @Test
    void boletoRejeitaValorAbaixoDoMinimo() {
        Formando f = new Formando("1", "A", "Computação", "a@ufal.br", "8290001");
        assertThrows(IllegalArgumentException.class,
                () -> new PagamentoBoleto().processar(f, new BigDecimal("1.00")));
    }

    @Test
    void pagamentoCartaoGeraComprovanteComParcelas() {
        Formando f = new Formando("1", "A", "Computação", "a@ufal.br", "8290001");
        ReciboPagamento recibo = new PagamentoCartao(3).processar(f, new BigDecimal("600.00"));
        assertTrue(recibo.getMeio().startsWith("Cartão"),
                "Meio de pagamento deve começar com 'Cartão'");
        assertTrue(recibo.getComprovante().contains("3x"),
                "Comprovante do cartão deve mencionar o número de parcelas");
    }

    @Test
    void trocarMeioPagamentoAlteraComprovante() {
        Formando f = new Formando("1", "A", "Computação", "a@ufal.br", "8290001");
        BigDecimal valor = new BigDecimal("500.00");

        String comprovantePix = new PagamentoPix().processar(f, valor).getComprovante();
        String comprovanteBoleto = new PagamentoBoleto().processar(f, valor).getComprovante();

        assertNotEquals(comprovantePix, comprovanteBoleto,
                "Meios de pagamento diferentes devem gerar comprovantes diferentes");
    }

    // ═══════════════════════════════════════════════════════════
    // HOT SPOT 3 — Evento (Template Method)
    // ═══════════════════════════════════════════════════════════

    @Test
    void colacaoDeGrauOrganizaComEtapasEspecificas() {
        Turma turma = turmaComTres();
        Cronograma c = new ColacaoGrau().organizar(turma);

        assertEquals("Colação de Grau", c.getEvento());
        assertEquals(3, c.getParticipantes()); // todos participam
        assertTrue(c.getCustoEstimado().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(c.getEtapas().stream().anyMatch(e -> e.contains("diploma")),
                "Colação deve ter entrega de diplomas");
    }

    @Test
    void baileConsideraSomenteAderentes() {
        Turma turma = turmaComTres(); // 2 aderentes, 1 não
        Cronograma c = new BaileFormatura().organizar(turma);

        assertEquals("Baile de Formatura", c.getEvento());
        // 2 aderentes × 2 (com acompanhante) = 4
        assertEquals(4, c.getParticipantes());
        assertTrue(c.getEtapas().stream().anyMatch(e -> e.toLowerCase().contains("dança") || e.toLowerCase().contains("pista")),
                "Baile deve ter pista de dança");
    }

    @Test
    void eventoRejeiteturmaVazia() {
        Turma vazia = turmaVazia();
        // Colação exige ao menos 1 formando
        assertThrows(IllegalStateException.class, () -> new ColacaoGrau().organizar(vazia));
    }

    @Test
    void baileRejeiteturmasSemAderentes() {
        Turma turma = new Turma("T1", "Teste", 2026);
        Formando f = new Formando("1", "X", "Teste", "x@x", "000");
        f.setAderiu(false);
        turma.adicionar(f);
        // Baile exige ao menos 1 aderente
        assertThrows(IllegalStateException.class, () -> new BaileFormatura().organizar(turma));
    }

    @Test
    void trocarEventoAlteraEtapasECusto() {
        Turma turma = turmaComTres();
        Cronograma colacao = new ColacaoGrau().organizar(turma);
        Cronograma baile = new BaileFormatura().organizar(turma);

        assertNotEquals(colacao.getCustoEstimado(), baile.getCustoEstimado(),
                "Eventos diferentes devem ter custos diferentes");
        assertNotEquals(colacao.getEtapas(), baile.getEtapas(),
                "Eventos diferentes devem ter programações diferentes");
    }

    // ═══════════════════════════════════════════════════════════
    // HOT SPOT 4 — RegraVotacao (Strategy)
    // ═══════════════════════════════════════════════════════════

    @Test
    void maioriaSimplesDeterminaVencedora() {
        List<Voto> votos = List.of(
                new Voto("1", "Buffet A"),
                new Voto("2", "Buffet A"),
                new Voto("3", "Buffet B")
        );
        ResultadoVotacao r = new MaioriaSimples().apurar(votos, 3);
        assertTrue(r.isAprovada());
        assertEquals("Buffet A", r.getOpcaoVencedora());
        assertEquals(2, r.getApuracao().get("Buffet A"));
    }

    @Test
    void quorumQualificadoReproveParticipacaoInsuficiente() {
        // Quórum exige 80% de participação, mas só 1 de 5 votou
        List<Voto> votos = List.of(new Voto("1", "A"));
        ResultadoVotacao r = new QuorumQualificado(0.8, 0.6).apurar(votos, 5);
        assertFalse(r.isAprovada(),
                "Deve reprovar quando quórum de participação não é atingido");
    }

    @Test
    void quorumQualificadoAprovaComParticipaçãoSuficiente() {
        // Quórum de 50%, maioria de 60%. 3 de 3 votam, 2 votam em A (66%)
        List<Voto> votos = List.of(
                new Voto("1", "A"),
                new Voto("2", "A"),
                new Voto("3", "B")
        );
        ResultadoVotacao r = new QuorumQualificado(0.5, 0.6).apurar(votos, 3);
        assertTrue(r.isAprovada());
        assertEquals("A", r.getOpcaoVencedora());
    }

    @Test
    void votoPonderadoConsideraPesos() {
        // X vota em A (peso 1), Y vota em B (peso 5) → B ganha
        List<Voto> votos = List.of(
                new Voto("1", "A", 1),
                new Voto("2", "B", 5)
        );
        ResultadoVotacao r = new VotoPonderado().apurar(votos, 2);
        assertTrue(r.isAprovada());
        assertEquals("B", r.getOpcaoVencedora());
        assertEquals(5, r.getApuracao().get("B"));
    }

    @Test
    void trocarRegraVotacaoAlteraDesfecho() {
        // Com maioria simples: A ganha (2 votos de 3).
        // Com quórum qualificado (90% maioria): A tem 66%, não atinge 90% → reprova.
        List<Voto> votos = List.of(
                new Voto("1", "A"),
                new Voto("2", "A"),
                new Voto("3", "B")
        );
        ResultadoVotacao maioria = new MaioriaSimples().apurar(votos, 3);
        ResultadoVotacao quorum = new QuorumQualificado(0.5, 0.9).apurar(votos, 3);

        assertTrue(maioria.isAprovada(), "Maioria simples deve aprovar com 2/3 votos");
        assertFalse(quorum.isAprovada(), "Quórum 90% deve reprovar com 66% da vencedora");
    }

    // ═══════════════════════════════════════════════════════════
    // HOT SPOT 5 — CanalNotificacao (Template Method)
    // ═══════════════════════════════════════════════════════════

    @Test
    void emailNotificaQuandoTemEmailValido() {
        Formando f = new Formando("1", "Ana", "Comp", "ana@ufal.br", "000");
        boolean enviou = new CanalEmail().notificar(f, "Teste");
        assertTrue(enviou, "E-mail deve ser enviado quando formando tem e-mail válido");
    }

    @Test
    void emailFalhaQuandoSemEmail() {
        Formando f = new Formando("1", "Ana", "Comp", null, "000");
        boolean enviou = new CanalEmail().notificar(f, "Teste");
        assertFalse(enviou, "E-mail não deve ser enviado quando formando não tem e-mail");
    }

    @Test
    void smsNotificaQuandoTemTelefone() {
        Formando f = new Formando("1", "Ana", "Comp", "x@x", "82999990001");
        boolean enviou = new CanalSMS().notificar(f, "Teste");
        assertTrue(enviou, "SMS deve ser enviado quando formando tem telefone");
    }

    @Test
    void smsFalhaComTelefoneVazio() {
        Formando f = new Formando("1", "Ana", "Comp", "x@x", "");
        boolean enviou = new CanalSMS().notificar(f, "Teste");
        assertFalse(enviou, "SMS não deve ser enviado com telefone vazio");
    }

    @Test
    void whatsAppNotificaQuandoTemTelefone() {
        Formando f = new Formando("1", "Ana", "Comp", "x@x", "82999990001");
        boolean enviou = new CanalWhatsApp().notificar(f, "Teste");
        assertTrue(enviou, "WhatsApp deve ser enviado quando formando tem telefone");
    }

    // ═══════════════════════════════════════════════════════════
    // HOT SPOT 6 — Pacote (Factory)
    // ═══════════════════════════════════════════════════════════

    @Test
    void fabricaListaTresPacotesPadrao() {
        FabricaPacote fabrica = new FabricaPacote();
        List<Pacote> disponiveis = fabrica.disponiveis();
        assertEquals(3, disponiveis.size(),
                "Fábrica default deve listar 3 pacotes (Foto, Vídeo, Álbum de Luxo)");
    }

    @Test
    void fabricaCriaPacotePorCodigo() {
        FabricaPacote fabrica = new FabricaPacote();
        Pacote foto = fabrica.criar("FOTO");
        assertNotNull(foto);
        assertEquals("FOTO", foto.codigo());
        assertTrue(foto.preco().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void fabricaRejeitaCodigoInexistente() {
        FabricaPacote fabrica = new FabricaPacote();
        assertThrows(IllegalArgumentException.class, () -> fabrica.criar("INEXISTENTE"));
    }

    @Test
    void fabricaPermiteRegistrarNovoPacote() {
        FabricaPacote fabrica = new FabricaPacote();
        fabrica.registrar(() -> new Pacote() {
            @Override public String codigo() { return "BRINDE"; }
            @Override public String nome() { return "Kit Brinde"; }
            @Override public BigDecimal preco() { return new BigDecimal("50.00"); }
            @Override public List<String> itens() { return List.of("Caneca", "Chaveiro"); }
        });

        assertEquals(4, fabrica.disponiveis().size(), "Após registro, deve ter 4 pacotes");
        Pacote brinde = fabrica.criar("BRINDE");
        assertEquals("Kit Brinde", brinde.nome());
    }

    // ═══════════════════════════════════════════════════════════
    // INTEGRAÇÃO — GestorFormatura (parte invariante)
    // ═══════════════════════════════════════════════════════════

    @Test
    void gestorOrquestraFluxoCompletoComHotsPotsPlugados() {
        Turma turma = turmaComTres();

        GestorFormatura gestor = new GestorFormatura(turma)
                .comRateio(new RateioIgualitario())
                .comRegraVotacao(new MaioriaSimples())
                .comCanal(new CanalEmail());

        // 1) Rateio
        Map<Formando, BigDecimal> cotas = gestor.ratear(new BigDecimal("300.00"));
        assertEquals(3, cotas.size());

        // 2) Pagamento
        ReciboPagamento recibo = gestor.receberPagamento(
                turma.getFormandos().get(0), new BigDecimal("100.00"), new PagamentoPix());
        assertNotNull(recibo.getComprovante());

        // 3) Evento
        Cronograma cronograma = gestor.organizarEvento(new ColacaoGrau());
        assertFalse(cronograma.getEtapas().isEmpty());

        // 4) Votação
        ResultadoVotacao resultado = gestor.deliberar(List.of(
                new Voto("1", "A"), new Voto("2", "A"), new Voto("3", "B")));
        assertTrue(resultado.isAprovada());

        // 5) Comunicação
        int enviados = gestor.comunicar("Teste de integração!");
        assertTrue(enviados > 0, "Ao menos uma notificação deve ser enviada");
    }

    @Test
    void gestorLancaExcecaoSemRateioConfigurado() {
        GestorFormatura gestor = new GestorFormatura(turmaComTres());
        assertThrows(IllegalStateException.class,
                () -> gestor.ratear(new BigDecimal("100.00")),
                "Deve lançar exceção quando estratégia de rateio não foi configurada");
    }

    @Test
    void gestorLancaExcecaoSemRegraVotacao() {
        GestorFormatura gestor = new GestorFormatura(turmaComTres());
        assertThrows(IllegalStateException.class,
                () -> gestor.deliberar(List.of(new Voto("1", "A"))),
                "Deve lançar exceção quando regra de votação não foi configurada");
    }

    @Test
    void gestorPermiteTrocarHotSpotsParaDiferentesFormaturas() {
        Turma turma = turmaComTres();

        // Formatura 1: igualitário + quórum qualificado
        GestorFormatura gestor1 = new GestorFormatura(turma)
                .comRateio(new RateioIgualitario())
                .comRegraVotacao(new QuorumQualificado(0.5, 0.9));

        // Formatura 2: por adesão + maioria simples
        GestorFormatura gestor2 = new GestorFormatura(turma)
                .comRateio(new RateioPorAdesao())
                .comRegraVotacao(new MaioriaSimples());

        Map<Formando, BigDecimal> cotas1 = gestor1.ratear(new BigDecimal("300.00"));
        Map<Formando, BigDecimal> cotas2 = gestor2.ratear(new BigDecimal("300.00"));

        // Os resultados devem ser diferentes porque as estratégias são diferentes
        assertNotEquals(cotas1, cotas2,
                "Formaturas com hot spots diferentes devem produzir resultados diferentes");
    }
}
