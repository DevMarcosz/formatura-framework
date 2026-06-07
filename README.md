<h1 align="center">🎓 Framework para Sistemas de Formatura Online</h1>

<p align="center">
  Um <strong>framework de domínio</strong> para construção de sistemas de formatura,
  baseado em <strong>microserviços</strong> e <strong>componentes de software</strong>,
  com <strong>6 pontos de extensão (hot spots)</strong>.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3.2">
  <img src="https://img.shields.io/badge/Spring%20Cloud-Gateway-6DB33F?logo=spring&logoColor=white" alt="Spring Cloud Gateway">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker Compose">
  <img src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black" alt="React 18">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="MIT License">
</p>

---

## 📌 Visão geral

Este projeto **não é um aplicativo de formatura — é um framework**. Ele captura a
**parte invariante** comum a qualquer sistema de formatura (cerca de 80% da lógica) e
expõe **pontos de extensão (hot spots)** que cada curso customiza. A partir do mesmo
núcleo é possível construir a *Formatura de Medicina*, de *Engenharia*, de *Direito* —
apenas "plugando" implementações diferentes dos hot spots.

Conceitos demonstrados na prática:

- **Frozen spots × Hot spots** (parte invariante × parte variante)
- **Inversão de controle** (o framework chama o código da aplicação)
- **Framework híbrido**: caixa-branca (herança) + caixa-preta (composição/interfaces)
- Padrões **Template Method**, **Strategy** e **Factory**

## 🧩 Os 6 hot spots

| # | Hot spot | Padrão | Implementações de exemplo |
|---|----------|--------|----------------------------|
| 1 | `EstrategiaRateio` | Strategy | Igualitário · Por Adesão · Proporcional à Renda |
| 2 | `MeioPagamento` | Template Method | Pix · Boleto · Cartão |
| 3 | `Evento` | Template Method | Colação de Grau · Baile · Missa |
| 4 | `RegraVotacao` | Strategy | Maioria Simples · Quórum Qualificado · Voto Ponderado |
| 5 | `CanalNotificacao` | Template Method | E-mail · SMS · WhatsApp |
| 6 | `Pacote` | Factory | Foto · Vídeo · Álbum de Luxo |

A **parte invariante** está em `GestorFormatura` (orquestra os hot spots por composição)
e nos *template methods* `final` (`processar`, `organizar`, `notificar`).

## 🏛️ Arquitetura

```text
                       Frontend (React)
                              │
                       API Gateway (8080)
   ┌──────────┬───────────────┼───────────────┬───────────────┐
auth (8086) turmas (8085) financeiro (8081) eventos (8082) comissao (8083) notificacao (8084)
                              │
                       framework-core  ← componente reutilizável (hot spots), usado por todos
```

Arquitetura de **microserviços** com um **API Gateway** como porta única de entrada.
Todos os serviços de domínio **reutilizam o componente `framework-core`** — onde ficam
os hot spots. Esse é o reuso de software no nível arquitetural.

| Módulo | Porta | Responsabilidade / hot spot |
|--------|:-----:|------------------------------|
| `framework-core` | — | Componente reutilizável: define os 6 hot spots |
| `api-gateway` | 8080 | Ponto único de entrada (roteamento) |
| `auth-service` | 8086 | Registro, login e validação de token |
| `turmas-service` | 8085 | Gestão de turmas e formandos |
| `financeiro-service` | 8081 | Rateio · Pagamento · Pacote |
| `eventos-service` | 8082 | Evento |
| `comissao-service` | 8083 | Votação |
| `notificacao-service` | 8084 | Notificação |

## 🛠️ Tecnologias

**Backend:** Java 17 · Spring Boot 3.2 · Spring Cloud Gateway · Maven multi-módulo · JUnit 5
**Frontend:** React 18 · Vite
**Infra:** Docker · Docker Compose

## 🚀 Como executar

### Backend (Docker — não requer Java/Maven instalados)

```bash
docker compose up --build
```

Gateway disponível em <http://localhost:8080>. Teste rápido:

```bash
curl http://localhost:8080/financeiro/pacotes
curl http://localhost:8080/eventos/tipos
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Interface em <http://localhost:5173> (exercita os 6 hot spots).

### Demonstração do framework isolado (requer JDK 17 + Maven)

```bash
mvn -pl framework-core -am compile
java -cp framework-core/target/classes br.ufal.ic.formatura.core.demo.DemoFramework
```

Cria **duas formaturas diferentes** (Medicina e Engenharia) a partir do mesmo núcleo.

## 🔌 Exemplos de requisições

Rateio (hot spot 1):

```bash
curl -X POST http://localhost:8080/financeiro/rateio \
  -H "Content-Type: application/json" \
  -d '{"custoTotal":30000,"estrategia":"IGUALITARIO",
       "formandos":[{"id":"1","nome":"Ana","aderiu":true},
                    {"id":"2","nome":"Bruno","aderiu":true}]}'
```

Organizar evento (hot spot 3):

```bash
curl -X POST http://localhost:8080/eventos/organizar \
  -H "Content-Type: application/json" \
  -d '{"tipo":"BAILE","formandos":[{"id":"1","nome":"Ana","aderiu":true}]}'
```

## 🧪 Testes

```bash
mvn -pl framework-core test
```

Os testes (`FrameworkTest`) validam a substituibilidade dos hot spots de rateio e
pagamento e o comportamento da parte invariante.

## 📂 Estrutura do repositório

```
formatura-framework/
├── pom.xml                  # Parent Maven (multi-módulo)
├── docker-compose.yml
├── framework-core/          # O FRAMEWORK (hot spots + parte invariante)
├── api-gateway/
├── auth-service/
├── turmas-service/
├── financeiro-service/
├── eventos-service/
├── comissao-service/
├── notificacao-service/
└── frontend/                # Interface web (React)
```

## 👤 Autor

**Marcos** — Engenharia de Computação, Universidade Federal de Alagoas (UFAL)
Disciplina ECOM189 — Reuso de Software e Metodologias Ágeis.

## 📝 Licença

Distribuído sob a licença MIT. Veja [`LICENSE`](LICENSE).
