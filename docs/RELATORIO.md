# Relatório do Projeto
## Um Framework para Sistemas de Formatura Online Baseados em MicroServices e Componentes de Software

**Universidade Federal de Alagoas — Instituto de Computação**
**Disciplina:** ECOM189 – Reuso de Software e Metodologias Ágeis (2026.1)
**Professor:** Dr. Arturo Hernández Domínguez
**Equipe:** Marcos

**Links:**
- Repositório GitHub: https://github.com/DevMarcosz/formatura-framework
- Vídeo de demonstração: `https://youtu.be/<id>` *(preencher)*
- Aplicação publicada (frontend): `https://<app>.vercel.app` *(preencher)*

---

## Sumário
1. Enunciado
2. Fundamentação: Frameworks, Hot Spots e Reuso
3. Product Backlog
4. Sprint Backlog
5. Arquitetura do Sistema
6. O Framework: Hot Spots e Frozen Spots
7. Microserviços Implementados
8. Diagramas UML-F
9. Tecnologias Utilizadas
10. Testes e Validação
11. Metodologia Ágil (Scrum)
12. Considerações Finais

---

## 1. Enunciado

Este projeto consiste no desenvolvimento de um **framework de domínio** para a
construção de **Sistemas de Formatura Online**, utilizando **arquitetura de
microserviços** e **componentes de software**, com **backend orientado a objetos**
(Java/Spring Boot) e **interface web** (React).

Diferentemente de um aplicativo único, um framework captura a **solução invariante**
de um conjunto de problemas de um domínio (organização de formaturas) e expõe
**pontos de extensão (hot spots)** que permitem gerar diferentes sistemas concretos
— a formatura de Medicina, de Engenharia, de Direito etc. — reaproveitando
~80% da lógica. O framework atende ao requisito mínimo de **4 hot spots**,
oferecendo **6**.

## 2. Fundamentação: Frameworks, Hot Spots e Reuso

Conforme apresentado em aula (Fayad & Schmidt, 1997; Fontoura et al., UML-F):

- Uma aplicação possui uma **parte invariante** (compartilhada por todas as
  aplicações do domínio) e uma **parte variante** (que a diferencia das demais).
- O framework registra a **solução invariante** e provê **comportamento default**.
- **Frozen spot** ↔ *template method* (parte fixa). **Hot spot** ↔ *hook method* /
  *variation point* (ponto de adaptação).
- **Inversão de controle:** é o código do framework que chama o código da
  aplicação (e não o contrário).
- Classificação: este é um **framework híbrido** — usa **caixa-branca** (herança,
  ex.: `Evento`, `MeioPagamento`) e **caixa-preta** (composição/interfaces, ex.:
  `EstrategiaRateio`, `RegraVotacao` injetadas no `GestorFormatura`).

## 3. Product Backlog

| ID | História | Prioridade | Estimativa |
|----|----------|------------|------------|
| 1 | Modelar o domínio de formaturas (Turma, Formando) | Muito alta | 6h |
| 2 | Projetar os hot spots do framework | Muito alta | 16h |
| 3 | Implementar a parte invariante (GestorFormatura, template methods) | Muito alta | 10h |
| 4 | API Gateway + roteamento | Alta | 8h |
| 5 | Microserviço de autenticação | Alta | 8h |
| 6 | Microserviços de domínio (financeiro, eventos, comissão, notificação, turmas) | Alta | 24h |
| 7 | Containerização (Docker Compose) | Média | 6h |
| 8 | Frontend web | Média | 16h |
| 9 | Testes do framework | Média | 6h |
| 10 | Documentação e diagramas UML-F | Média | 8h |

## 4. Sprint Backlog

### Sprint 1 — Núcleo do framework
- Domínio (`Turma`, `Formando`, value objects).
- 6 hot spots com 2–3 implementações concretas cada.
- `GestorFormatura` (parte invariante) + demo de duas formaturas distintas.
- Testes JUnit dos hot spots.

### Sprint 2 — Microserviços e infraestrutura
- API Gateway (Spring Cloud Gateway).
- auth, turmas, financeiro, eventos, comissão, notificação.
- Docker Compose orquestrando todos os serviços.

### Sprint 3 — Frontend e fechamento
- Interface React (cadastro de turma, rateio, eventos, votação, notificações).
- Deploy do frontend, vídeo de demonstração, relatório final.

## 5. Arquitetura do Sistema

Arquitetura de microserviços com **API Gateway** como ponto único de entrada.
Todos os microserviços de domínio **reutilizam o componente `framework-core`**,
que concentra os hot spots — é o reuso de software no nível arquitetural.

```
Frontend (React)
      │
API Gateway (8080) ── roteia por path
      ├── auth-service (8086)
      ├── turmas-service (8085)
      ├── financeiro-service (8081)   ─┐
      ├── eventos-service (8082)       ├─ usam framework-core
      ├── comissao-service (8083)      │  (hot spots)
      └── notificacao-service (8084)  ─┘
```

**Características:** escalabilidade (cada serviço escala isolado),
manutenibilidade (responsabilidades separadas), resiliência (falha isolada) e
**reuso** (o framework é dependência comum, versionada, de todos).

## 6. O Framework: Hot Spots e Frozen Spots

> Esta é a seção central da disciplina. Cada hot spot é um ponto de adaptação;
> cada template method `final` é um frozen spot.

### Hot Spot 1 — `EstrategiaRateio` (Strategy)
Decide como o custo total é dividido. Interface:
```java
public interface EstrategiaRateio {
    Map<Formando, BigDecimal> calcular(Turma turma, BigDecimal custoTotal);
    String nome();
}
```
Implementações: `RateioIgualitario`, `RateioPorAdesao`, `RateioProporcionalRenda`.

### Hot Spot 2 — `MeioPagamento` (Template Method)
`processar()` é o frozen spot (valida → cobra → registra → recibo). `executarCobranca()`
é o hot spot abstrato.
```java
public final ReciboPagamento processar(Formando f, BigDecimal valor) {
    validar(f, valor);
    String comprovante = executarCobranca(f, valor); // hot spot
    registrar(f, valor, comprovante);
    return new ReciboPagamento(...);
}
protected abstract String executarCobranca(Formando f, BigDecimal valor);
```
Implementações: `PagamentoPix`, `PagamentoBoleto`, `PagamentoCartao`.

### Hot Spot 3 — `Evento` (Template Method)
`organizar()` define o esqueleto invariante; `calcularCusto`, `validarParticipantes`
e `montarProgramacao` são os hot spots. Implementações: `ColacaoGrau`,
`BaileFormatura`, `MissaFormatura`.

### Hot Spot 4 — `RegraVotacao` (Strategy)
Apuração das deliberações da comissão. Implementações: `MaioriaSimples`,
`QuorumQualificado`, `VotoPonderado`.

### Hot Spot 5 — `CanalNotificacao` (Template Method)
`notificar()` invariante (verifica disponibilidade → formata → envia). Hot spots:
`disponivel`, `enviar`. Implementações: `CanalEmail`, `CanalSMS`, `CanalWhatsApp`.

### Hot Spot 6 — `Pacote` (Factory)
Produtos comercializados, registráveis em tempo de execução via `FabricaPacote`
(Open-Closed). Implementações: `PacoteFoto`, `PacoteVideo`, `PacoteAlbumLuxo`.

### Parte invariante — `GestorFormatura`
Orquestra os hot spots por composição (caixa-preta) e demonstra **inversão de
controle**: é o gestor que chama as implementações fornecidas pela aplicação.
A classe `DemoFramework` cria **duas formaturas diferentes** plugando hot spots
distintos — equivalente ao exemplo `Aplicacao1`/`Aplicacao2` visto em aula.

## 7. Microserviços Implementados

Para cada serviço, descreva endpoints e responsabilidades (resumo):

- **api-gateway** — roteamento por path para os demais serviços.
- **auth-service** — `POST /auth/register`, `/auth/login`, `/auth/verify`.
- **turmas-service** — CRUD de turmas/formandos (`/turmas`, `/turmas/{id}/formandos`).
- **financeiro-service** — `/financeiro/pacotes`, `/financeiro/rateio`, `/financeiro/pagamento`.
- **eventos-service** — `/eventos/tipos`, `/eventos/organizar`.
- **comissao-service** — `/comissao/regras`, `/comissao/apurar`.
- **notificacao-service** — `/notificacoes/canais`, `/notificacoes/enviar`.

## 8. Diagramas UML-F

Ver [`DIAGRAMAS-UML-F.md`](DIAGRAMAS-UML-F.md). Incluem:
- Diagrama de componentes/microserviços.
- Diagrama UML-F de cada hot spot (estereótipos `<<framework>>`, `<<application>>`,
  `<< adapt-static >>`, `<< fixed >>`).

## 9. Tecnologias Utilizadas

Java 17, Spring Boot 3.2, Spring Cloud Gateway, Maven multi-módulo, Docker /
Docker Compose, React, JUnit 5, Git/GitHub.

## 10. Testes e Validação

### 10.1 Testes automatizados (JUnit 5)

Arquivo: `framework-core/src/test/.../FrameworkTest.java` — **27 testes** cobrindo todos os 6 hot spots:

| Hot Spot | Testes | O que valida |
|---|---|---|
| 1 — EstrategiaRateio | 4 | Igualitário, por adesão, proporcional à renda, substituibilidade |
| 2 — MeioPagamento | 4 | Pix, boleto (rejeição), cartão (parcelas), troca de meio |
| 3 — Evento | 5 | Colação, baile (aderentes), turma vazia, sem aderentes, troca de evento |
| 4 — RegraVotacao | 5 | Maioria simples, quórum insuficiente, quórum suficiente, voto ponderado, troca de regra |
| 5 — CanalNotificacao | 5 | Email com/sem contato, SMS com/sem telefone, WhatsApp |
| 6 — Pacote/FabricaPacote | 4 | Listagem, criação, código inválido, registro de novo pacote |
| Integração (GestorFormatura) | 4 | Fluxo completo, exceções sem configuração, troca de hot spots |

Para executar:

```bash
mvn -pl framework-core test
```

### 10.2 Testes manuais via Gateway (curl)

> Todos os exemplos assumem `docker compose up --build` rodando.

**Hot Spot 1 — Rateio (Strategy):**

```bash
curl -s -X POST http://localhost:8080/financeiro/rateio \
  -H "Content-Type: application/json" \
  -d '{
    "custoTotal": 30000,
    "estrategia": "IGUALITARIO",
    "formandos": [
      {"id":"1","nome":"Ana","email":"ana@ufal.br","telefone":"82999990001","aderiu":true,"renda":3000},
      {"id":"2","nome":"Bruno","email":"bruno@ufal.br","telefone":"82999990002","aderiu":true,"renda":5000},
      {"id":"3","nome":"Carla","email":"carla@ufal.br","telefone":"82999990003","aderiu":false,"renda":2000}
    ]
  }' | python -m json.tool
```

Saída esperada:
```json
[
    {"formando": "Ana",   "valor": 10000.00},
    {"formando": "Bruno", "valor": 10000.00},
    {"formando": "Carla", "valor": 10000.00}
]
```

**Hot Spot 2 — Pagamento (Template Method):**

```bash
curl -s -X POST http://localhost:8080/financeiro/pagamento \
  -H "Content-Type: application/json" \
  -d '{"formandoId":"1","nome":"Ana","valor":10000,"meio":"PIX","parcelas":1}' \
  | python -m json.tool
```

Saída esperada:
```json
{
    "formandoId": "1",
    "valor": 10000,
    "meio": "Pix",
    "comprovante": "PIX-A1B2C3D4",
    "quando": "2026-06-06T18:30:00.000"
}
```

**Hot Spot 3 — Evento (Template Method):**

```bash
curl -s -X POST http://localhost:8080/eventos/organizar \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "COLACAO",
    "formandos": [
      {"id":"1","nome":"Ana","aderiu":true},
      {"id":"2","nome":"Bruno","aderiu":true}
    ]
  }' | python -m json.tool
```

Saída esperada:
```json
{
    "evento": "Colação de Grau",
    "custoEstimado": 240.00,
    "participantes": 2,
    "etapas": [
        "Reserva do auditório/teatro oficial da instituição",
        "Entrada solene dos formandos",
        "Juramento da turma",
        "Entrega de diplomas",
        "Discurso do paraninfo",
        "Encerramento de Colação de Grau"
    ]
}
```

**Hot Spot 4 — Votação (Strategy):**

```bash
curl -s -X POST http://localhost:8080/comissao/apurar \
  -H "Content-Type: application/json" \
  -d '{
    "regra": "MAIORIA",
    "totalAptos": 3,
    "votos": [
      {"formandoId":"1","opcao":"Buffet A","peso":1},
      {"formandoId":"2","opcao":"Buffet A","peso":1},
      {"formandoId":"3","opcao":"Buffet B","peso":1}
    ]
  }' | python -m json.tool
```

Saída esperada:
```json
{
    "opcaoVencedora": "Buffet A",
    "aprovada": true,
    "apuracao": {"Buffet A": 2, "Buffet B": 1},
    "observacao": "Maioria simples: 2 voto(s)"
}
```

**Hot Spot 5 — Notificação (Template Method):**

```bash
curl -s -X POST http://localhost:8080/notificacoes/enviar \
  -H "Content-Type: application/json" \
  -d '{
    "canal": "EMAIL",
    "mensagem": "A formatura foi confirmada!",
    "formandos": [
      {"id":"1","nome":"Ana","email":"ana@ufal.br","telefone":"82999990001","aderiu":true,"renda":0}
    ]
  }' | python -m json.tool
```

Saída esperada:
```json
{
    "canal": "E-mail",
    "total": 1,
    "enviados": 1
}
```

**Hot Spot 6 — Pacotes (Factory):**

```bash
curl -s http://localhost:8080/financeiro/pacotes | python -m json.tool
```

Saída esperada:
```json
[
    {
        "codigo": "FOTO",
        "nome": "Pacote Fotografia",
        "preco": 450.00,
        "itens": ["Cobertura da colação", "50 fotos digitais", "Ensaio individual"]
    },
    {
        "codigo": "VIDEO",
        "nome": "Pacote Filmagem",
        "preco": 700.00,
        "itens": ["Filmagem da colação", "Clipe de 5 minutos", "Drone aéreo"]
    },
    {
        "codigo": "ALBUM_LUXO",
        "nome": "Álbum de Luxo",
        "preco": 1200.00,
        "itens": ["Álbum 30x30 capa dura", "100 fotos impressas", "Ensaio externo", "Caixa personalizada"]
    }
]
```

## 11. Metodologia Ágil (Scrum)

Projeto conduzido em 3 sprints (seção 4), com Product Backlog priorizado, entregas
incrementais e revisão ao fim de cada sprint. O reuso do `framework-core` reduziu o
esforço de cada microserviço, evidenciando o ganho do desenvolvimento ágil baseado
em reuso.

## 12. Considerações Finais

O projeto entrega um framework de domínio funcional, com 6 hot spots (acima do
mínimo de 4), arquitetura de microserviços, componente reutilizável compartilhado e
interface web. Demonstra na prática os conceitos de frozen/hot spots, inversão de
controle e frameworks híbridos (caixa-branca + caixa-preta).

**Melhorias futuras:** persistência em banco por serviço, JWT real no gateway,
descoberta de serviços (Eureka), e novos hot spots (ex.: contratos com fornecedores).
