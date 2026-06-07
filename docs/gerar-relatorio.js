const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, LevelFormat, HeadingLevel, BorderStyle, WidthType,
  ShadingType, TableOfContents, PageBreak, ExternalHyperlink, VerticalAlign,
  ImageRun,
} = require("docx");

// ---- Constantes de layout (A4, margens ~ABNT) -------------------------
const PAGE_W = 11906, PAGE_H = 16838;
const M_TOP = 1701, M_LEFT = 1701, M_RIGHT = 1134, M_BOTTOM = 1134;
const CONTENT_W = PAGE_W - M_LEFT - M_RIGHT; // 9071

// ---- Helpers ----------------------------------------------------------
const P = (text, opts = {}) =>
  new Paragraph({ spacing: { after: 120, line: 276 }, ...opts,
    children: opts.children || [new TextRun({ text, size: 24 })] });

const H1 = (text) => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun(text)] });
const H2 = (text) => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(text)] });
const H3 = (text) => new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun(text)] });

const bullet = (text) => new Paragraph({
  numbering: { reference: "bullets", level: 0 }, spacing: { after: 60 },
  children: [new TextRun({ text, size: 24 })] });

const codeBlock = (lines) => lines.map((ln, i) => new Paragraph({
  shading: { type: ShadingType.CLEAR, fill: "F2F2F2" },
  spacing: { after: i === lines.length - 1 ? 160 : 0, line: 240 },
  children: [new TextRun({ text: ln === "" ? " " : ln, font: "Consolas", size: 18 })] }));

const border = { style: BorderStyle.SINGLE, size: 1, color: "BBBBBB" };
const borders = { top: border, bottom: border, left: border, right: border };

function table(widths, rows) {
  return new Table({
    width: { size: CONTENT_W, type: WidthType.DXA },
    columnWidths: widths,
    rows: rows.map((cells, ri) => new TableRow({
      tableHeader: ri === 0,
      children: cells.map((c, ci) => new TableCell({
        borders, width: { size: widths[ci], type: WidthType.DXA },
        verticalAlign: VerticalAlign.CENTER,
        shading: ri === 0 ? { type: ShadingType.CLEAR, fill: "6C5CE7" } : undefined,
        margins: { top: 60, bottom: 60, left: 110, right: 110 },
        children: [new Paragraph({
          spacing: { after: 0 },
          children: [new TextRun({ text: String(c), size: 20, bold: ri === 0,
            color: ri === 0 ? "FFFFFF" : "000000" })] })],
      })),
    })),
  });
}

const center = (runs, opts = {}) => new Paragraph({
  alignment: AlignmentType.CENTER, spacing: { after: 120 }, ...opts, children: runs });

// Lê largura/altura de um PNG (chunk IHDR) para preservar a proporção.
function pngSize(file) {
  const b = fs.readFileSync(file);
  return { w: b.readUInt32BE(16), h: b.readUInt32BE(20) };
}

// Insere uma imagem centralizada com legenda, escalada para caber na página.
function imagem(file, legenda, maxW = 580) {
  const { w, h } = pngSize(file);
  const scale = Math.min(1, maxW / w);
  const width = Math.round(w * scale);
  const height = Math.round(h * scale);
  const out = [new Paragraph({
    alignment: AlignmentType.CENTER, spacing: { before: 100, after: 40 },
    children: [new ImageRun({
      type: "png", data: fs.readFileSync(file),
      transformation: { width, height },
      altText: { title: legenda, description: legenda, name: legenda },
    })],
  })];
  if (legenda) {
    out.push(new Paragraph({
      alignment: AlignmentType.CENTER, spacing: { after: 200 },
      children: [new TextRun({ text: legenda, italics: true, size: 18, color: "666666" })],
    }));
  }
  return out;
}

// ---- CAPA -------------------------------------------------------------
const capa = [
  center([new TextRun({ text: "UNIVERSIDADE FEDERAL DE ALAGOAS", bold: true, size: 28 })], { spacing: { before: 400, after: 60 } }),
  center([new TextRun({ text: "INSTITUTO DE COMPUTAÇÃO", bold: true, size: 28 })], { spacing: { after: 40 } }),
  center([new TextRun({ text: "Engenharia de Computação", size: 24 })], { spacing: { after: 1600 } }),
  center([new TextRun({ text: "Um Framework para Sistemas de Formatura Online", bold: true, size: 40 })], { spacing: { after: 80 } }),
  center([new TextRun({ text: "Baseados em MicroServices e Componentes de Software", bold: true, size: 32 })], { spacing: { after: 1400 } }),
  center([new TextRun({ text: "Marcos", size: 26 })], { spacing: { after: 1400 } }),
  center([new TextRun({ text: "Disciplina: ECOM189 — Reuso de Software e Metodologias Ágeis", size: 22 })], { spacing: { after: 40 } }),
  center([new TextRun({ text: "Prof. Dr. Arturo Hernández Domínguez", size: 22 })], { spacing: { after: 1400 } }),
  center([new TextRun({ text: "Maceió — AL", size: 24 })], { spacing: { after: 20 } }),
  center([new TextRun({ text: "2026", size: 24 })]),
  new Paragraph({ children: [new PageBreak()] }),
];

// ---- Página de links --------------------------------------------------
const linkLine = (label, url) => new Paragraph({
  alignment: AlignmentType.CENTER, spacing: { after: 60 },
  children: [
    new TextRun({ text: label + " ", bold: true, size: 22 }),
    new ExternalHyperlink({ link: url, children: [new TextRun({ text: url, style: "Hyperlink", size: 22 })] }),
  ] });

const rosto = [
  center([new TextRun({ text: "Relatório do Projeto", bold: true, size: 32 })], { spacing: { before: 200, after: 200 } }),
  new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 300 },
    children: [new TextRun({ size: 24, text:
      "Este relatório apresenta o framework desenvolvido para a disciplina de Reuso de Software e Metodologias Ágeis, ministrada pelo Prof. Dr. Arturo Hernández Domínguez. O projeto consiste em um framework de domínio para sistemas de formatura online, construído com arquitetura de microserviços e componentes de software, com backend orientado a objetos e interface web." })] }),
  H2("Links do Projeto"),
  linkLine("Repositório GitHub:", "https://github.com/DevMarcosz/formatura-framework"),
  linkLine("Vídeo de demonstração:", "https://youtu.be/<id>"),
  linkLine("Aplicação publicada:", "https://<app>.vercel.app"),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 120 },
    children: [new TextRun({ text: "(vídeo e aplicação publicada: substituir antes da entrega)", italics: true, size: 18, color: "888888" })] }),
  new Paragraph({ children: [new PageBreak()] }),
];

// ---- Sumário ----------------------------------------------------------
const sumario = [
  H1("Sumário"),
  new TableOfContents("Sumário", { hyperlink: true, headingStyleRange: "1-3" }),
  new Paragraph({ children: [new PageBreak()] }),
];

// ---- Corpo ------------------------------------------------------------
const corpo = [
  H1("1. Enunciado"),
  P("Este projeto consiste no desenvolvimento de um framework de domínio para a construção de Sistemas de Formatura Online, utilizando arquitetura de microserviços e componentes de software, com backend orientado a objetos (Java/Spring Boot) e interface web (React)."),
  P("Diferentemente de um aplicativo único, um framework captura a solução invariante de um conjunto de problemas de um domínio — neste caso, a organização de formaturas — e expõe pontos de extensão (hot spots) que permitem gerar diferentes sistemas concretos: a formatura de Medicina, de Engenharia, de Direito, etc. O framework fornece aproximadamente 80% das funcionalidades, reaproveitadas por todas as aplicações do domínio."),
  P("O requisito mínimo de 4 hot spots foi atendido com folga: o framework oferece 6 pontos de extensão."),

  H1("2. Fundamentação: Frameworks, Hot Spots e Reuso"),
  P("Conforme apresentado em aula (Fayad & Schmidt, 1997; Fontoura et al., UML-F), uma aplicação possui uma parte invariante (compartilhada por todas as aplicações do domínio) e uma parte variante (que a diferencia das demais). O framework registra a solução invariante e provê comportamento default."),
  bullet("Frozen spot ↔ template method: a parte fixa do algoritmo."),
  bullet("Hot spot ↔ hook method / variation point: o ponto de adaptação preenchido pela aplicação."),
  bullet("Inversão de controle: é o código do framework que chama o código da aplicação, e não o contrário."),
  P("Classificação: este é um framework híbrido — utiliza caixa-branca (herança, ex.: Evento e MeioPagamento, com sobrescrita de métodos) e caixa-preta (composição/interfaces, ex.: EstrategiaRateio e RegraVotacao, injetadas no GestorFormatura)."),

  H1("3. Product Backlog"),
  table([720, 5351, 1500, 1500], [
    ["ID", "História", "Prioridade", "Estimativa"],
    ["1", "Modelar o domínio de formaturas (Turma, Formando)", "Muito alta", "6h"],
    ["2", "Projetar os hot spots do framework", "Muito alta", "16h"],
    ["3", "Implementar a parte invariante (GestorFormatura, template methods)", "Muito alta", "10h"],
    ["4", "API Gateway + roteamento", "Alta", "8h"],
    ["5", "Microserviço de autenticação", "Alta", "8h"],
    ["6", "Microserviços de domínio (financeiro, eventos, comissão, notificação, turmas)", "Alta", "24h"],
    ["7", "Containerização (Docker Compose)", "Média", "6h"],
    ["8", "Frontend web (React)", "Média", "16h"],
    ["9", "Testes do framework (JUnit)", "Média", "6h"],
    ["10", "Documentação e diagramas UML-F", "Média", "8h"],
  ]),

  H1("4. Sprint Backlog"),
  H2("Sprint 1 — Núcleo do framework"),
  table([720, 6851, 1500], [
    ["ID", "Funcionalidade", "Nível"],
    ["1", "Domínio (Turma, Formando, objetos de valor)", "Alta"],
    ["2", "6 hot spots com 2–3 implementações concretas cada", "Alta"],
    ["3", "GestorFormatura (parte invariante) + DemoFramework", "Alta"],
    ["4", "Testes JUnit dos hot spots", "Média"],
  ]),
  H2("Sprint 2 — Microserviços e infraestrutura"),
  table([720, 6851, 1500], [
    ["ID", "Funcionalidade", "Nível"],
    ["5", "API Gateway (Spring Cloud Gateway)", "Alta"],
    ["6", "Serviços auth, turmas, financeiro, eventos, comissão, notificação", "Alta"],
    ["7", "Orquestração com Docker Compose", "Média"],
  ]),
  H2("Sprint 3 — Frontend e fechamento"),
  table([720, 6851, 1500], [
    ["ID", "Funcionalidade", "Nível"],
    ["8", "Interface React (rateio, eventos, votação)", "Média"],
    ["9", "Deploy do frontend e vídeo de demonstração", "Média"],
    ["10", "Relatório final e diagramas UML-F", "Média"],
  ]),

  H1("5. Arquitetura do Sistema"),
  P("A arquitetura segue o padrão de microserviços com um API Gateway como ponto único de entrada. Todos os microserviços de domínio reutilizam o componente framework-core, que concentra os hot spots — este é o reuso de software no nível arquitetural."),
  ...imagem("diagramas/Diagrama de componentes.png", "Figura 1 — Diagrama de componentes (microserviços e o componente framework-core).", 560),
  P("Características da arquitetura:"),
  bullet("Escalabilidade: cada microserviço escala de forma independente."),
  bullet("Manutenibilidade: responsabilidades separadas por serviço."),
  bullet("Resiliência: a falha de um serviço não derruba os demais."),
  bullet("Reuso: o framework-core é dependência comum e versionada de todos os serviços."),

  H1("6. O Framework: Hot Spots e Frozen Spots"),
  P("Esta é a seção central da disciplina. Cada hot spot é um ponto de adaptação; cada template method final é um frozen spot. A seguir, os 6 hot spots implementados."),

  H2("6.1. Hot Spot 1 — EstrategiaRateio (Strategy)"),
  P("Decide como o custo total é dividido entre os formandos. Implementações: RateioIgualitario, RateioPorAdesao e RateioProporcionalRenda."),
  ...codeBlock([
    "public interface EstrategiaRateio {",
    "    Map<Formando, BigDecimal> calcular(Turma turma, BigDecimal custoTotal);",
    "    String nome();",
    "}",
  ]),

  H2("6.2. Hot Spot 2 — MeioPagamento (Template Method)"),
  P("O método processar() é o frozen spot (final): valida, dispara a cobrança, registra e gera o recibo, sempre nessa ordem. O passo executarCobranca() é o hot spot abstrato. Implementações: PagamentoPix, PagamentoBoleto e PagamentoCartao."),
  ...codeBlock([
    "public final ReciboPagamento processar(Formando f, BigDecimal valor) {",
    "    validar(f, valor);",
    "    String comprovante = executarCobranca(f, valor); // hot spot",
    "    registrar(f, valor, comprovante);",
    "    return new ReciboPagamento(...);",
    "}",
    "protected abstract String executarCobranca(Formando f, BigDecimal valor);",
  ]),

  H2("6.3. Hot Spot 3 — Evento (Template Method)"),
  P("organizar() define o esqueleto invariante de organização; calcularCusto, validarParticipantes e montarProgramacao são os hot spots. Implementações: ColacaoGrau, BaileFormatura e MissaFormatura."),

  H2("6.4. Hot Spot 4 — RegraVotacao (Strategy)"),
  P("Apuração das deliberações da comissão de formatura. Implementações: MaioriaSimples, QuorumQualificado e VotoPonderado."),

  H2("6.5. Hot Spot 5 — CanalNotificacao (Template Method)"),
  P("notificar() é a parte invariante (verifica disponibilidade, formata e envia). Os hot spots são disponivel e enviar. Implementações: CanalEmail, CanalSMS e CanalWhatsApp."),

  H2("6.6. Hot Spot 6 — Pacote (Factory)"),
  P("Produtos comercializados na formatura, registráveis em tempo de execução via FabricaPacote (princípio Open-Closed). Implementações: PacoteFoto, PacoteVideo e PacoteAlbumLuxo."),

  H2("6.7. Parte invariante — GestorFormatura"),
  P("O GestorFormatura orquestra os hot spots por composição (framework caixa-preta) e demonstra a inversão de controle: é ele quem chama as implementações fornecidas pela aplicação. A classe DemoFramework cria duas formaturas diferentes (Medicina e Engenharia) a partir do mesmo núcleo, apenas plugando hot spots distintos — equivalente ao exemplo Aplicacao1/Aplicacao2 visto em aula."),

  H1("7. Microserviços Implementados"),
  table([2300, 1100, 5671], [
    ["Serviço", "Porta", "Endpoints / responsabilidade"],
    ["api-gateway", "8080", "Roteamento por path para os demais serviços"],
    ["auth-service", "8086", "POST /auth/register, /auth/login, /auth/verify"],
    ["turmas-service", "8085", "/turmas, /turmas/{id}/formandos (CRUD)"],
    ["financeiro-service", "8081", "/financeiro/pacotes, /rateio, /pagamento"],
    ["eventos-service", "8082", "/eventos/tipos, /eventos/organizar"],
    ["comissao-service", "8083", "/comissao/regras, /comissao/apurar"],
    ["notificacao-service", "8084", "/notificacoes/canais, /notificacoes/enviar"],
  ]),

  H1("8. Diagramas UML-F"),
  P("A especificação do framework utiliza a notação UML-F (Fontoura, Pree, Rumpe), com os estereótipos «framework», «application», «adapt-static» (operação adaptada por herança ou composição) e «fixed» (template method / frozen spot). As figuras a seguir apresentam os principais hot spots e a parte invariante."),
  ...imagem("diagramas/hot spot 2.png", "Figura 2 — Hot Spot 2: MeioPagamento (Template Method, caixa-branca). processar() é «fixed» e executarCobranca() é «adapt-static».", 560),
  ...imagem("diagramas/hot spot 3.png", "Figura 3 — Hot Spot 3: Evento (Template Method). organizar() é o frozen spot; calcularCusto, validarParticipantes e montarProgramacao são os hot spots.", 440),
  ...imagem("diagramas/hot spot 1 e 4.png", "Figura 4 — Hot Spots 1 e 4: EstrategiaRateio e RegraVotacao (Strategy, caixa-preta).", 600),
  ...imagem("diagramas/invariante.png", "Figura 5 — Parte invariante: GestorFormatura orquestra os hot spots por composição, demonstrando a inversão de controle.", 560),

  H1("9. Tecnologias Utilizadas"),
  bullet("Java 17 e Spring Boot 3.2 — backend orientado a objetos (obrigatório)."),
  bullet("Spring Cloud Gateway — padrão API Gateway."),
  bullet("Maven multi-módulo — reuso do componente framework-core."),
  bullet("Docker e Docker Compose — cada microserviço em um container."),
  bullet("React (Vite) — interface web."),
  bullet("JUnit 5 — testes automatizados do framework."),
  bullet("Git / GitHub — controle de versão."),

  H1("10. Testes e Validação"),
  H2("10.1. Testes automatizados (JUnit 5)"),
  P("A classe FrameworkTest cobre: rateio igualitário, rateio por adesão (isenção de quem não aderiu), geração de comprovante Pix e rejeição de boleto abaixo do valor mínimo."),
  H2("10.2. Validação de integração (via API Gateway)"),
  P("Com os 7 contêineres em execução, os endpoints foram exercitados através do gateway na porta 8080, com os seguintes resultados reais:"),
  table([3400, 3171, 2500], [
    ["Requisição", "Resultado", "Hot spot validado"],
    ["GET /financeiro/pacotes", "3 pacotes retornados", "6 — Pacote (Factory)"],
    ["POST /financeiro/rateio (Igualitário, R$30.000, 3 formandos)", "R$ 10.000 por formando", "1 — Rateio (Strategy)"],
    ["POST /eventos/organizar (Baile, 2 aderentes)", "Custo R$ 8.360 e 6 etapas", "3 — Evento (Template Method)"],
    ["POST /comissao/apurar (Quórum, 2/4 votos)", "Aprovada (50% quórum, 100% maioria)", "4 — Votação (Strategy)"],
  ]),
  P("O cálculo do baile confirma a lógica do template method: R$ 8.000 (espaço) + R$ 90 × 4 participantes (2 aderentes, cada um com acompanhante) = R$ 8.360. A apuração por quórum aplicou corretamente os limites de 50% de participação e 60% de maioria."),

  H1("11. Metodologia Ágil (Scrum)"),
  P("O projeto foi conduzido em três sprints (seção 4), com Product Backlog priorizado, entregas incrementais e revisão ao fim de cada sprint. O reuso do framework-core reduziu significativamente o esforço de cada microserviço, evidenciando na prática o ganho do desenvolvimento ágil baseado em reuso de software."),

  H1("12. Considerações Finais"),
  P("O projeto entrega um framework de domínio funcional, com 6 hot spots (acima do mínimo de 4 exigido), arquitetura de microserviços, componente reutilizável compartilhado e interface web. Foram demonstrados, na prática, os conceitos de frozen spots e hot spots, inversão de controle e frameworks híbridos (caixa-branca combinada com caixa-preta)."),
  P("Aprendizados: separação entre parte invariante e variante, aplicação dos padrões Template Method, Strategy e Factory como mecanismos de extensão, e o papel dos microserviços como unidades de implantação que reutilizam um mesmo componente de framework."),
  P("Melhorias futuras: persistência em banco de dados por serviço, autenticação JWT real no gateway, descoberta de serviços (Eureka) e novos hot spots (por exemplo, contratos com fornecedores e modelos de convite)."),
];

// ---- Documento --------------------------------------------------------
const doc = new Document({
  styles: {
    default: { document: { run: { font: "Arial", size: 24 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 30, bold: true, font: "Arial", color: "1F1F3D" },
        paragraph: { spacing: { before: 280, after: 160 }, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 26, bold: true, font: "Arial", color: "2E2E5C" },
        paragraph: { spacing: { before: 200, after: 120 }, outlineLevel: 1 } },
      { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 24, bold: true, font: "Arial" },
        paragraph: { spacing: { before: 160, after: 100 }, outlineLevel: 2 } },
    ],
  },
  numbering: {
    config: [
      { reference: "bullets", levels: [{ level: 0, format: LevelFormat.BULLET, text: "•",
        alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 280 } } } }] },
    ],
  },
  sections: [{
    properties: { page: { size: { width: PAGE_W, height: PAGE_H },
      margin: { top: M_TOP, right: M_RIGHT, bottom: M_BOTTOM, left: M_LEFT } } },
    children: [...capa, ...rosto, ...sumario, ...corpo],
  }],
});

const saida = process.argv[2] || "Relatorio-Framework-Formatura.docx";
Packer.toBuffer(doc).then((buffer) => {
  fs.writeFileSync(saida, buffer);
  console.log("OK: " + saida + " gerado.");
});
