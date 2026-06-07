# Diagramas UML-F

Notação UML-F (Fontoura, Pree, Rumpe) — estereótipos:
- `<<framework>>` classe/operação que pertence ao framework
- `<<application>>` classe fornecida pela aplicação (parte variante)
- `<< adapt-static >>` operação adaptada por herança/sobrescrita (hot spot)
- `<< fixed >>` operação fixa (frozen spot / template method)

Os blocos abaixo estão em PlantUML — cole em <https://www.plantuml.com/plantuml>
para renderizar (ou use a extensão PlantUML do VS Code) e exporte como imagem
para o relatório final.

---

## 1. Diagrama de Componentes (Microserviços)

```plantuml
@startuml
skinparam componentStyle rectangle
actor Usuario
[Frontend React] as FE
[API Gateway\n:8080] as GW
[auth-service\n:8086] as AUTH
[turmas-service\n:8085] as TUR
[financeiro-service\n:8081] as FIN
[eventos-service\n:8082] as EVT
[comissao-service\n:8083] as COM
[notificacao-service\n:8084] as NOT
[framework-core\n(componente)] as CORE

Usuario --> FE
FE --> GW
GW --> AUTH
GW --> TUR
GW --> FIN
GW --> EVT
GW --> COM
GW --> NOT
TUR ..> CORE : usa
FIN ..> CORE : usa
EVT ..> CORE : usa
COM ..> CORE : usa
NOT ..> CORE : usa
@enduml
```

## 2. Hot Spot 2 — `MeioPagamento` (Template Method / caixa-branca)

```plantuml
@startuml
abstract class MeioPagamento <<framework>> {
  +processar(f, valor) <<fixed>>
  #validar(f, valor)
  #registrar(f, valor, c)
  #{abstract} executarCobranca(f, valor) <<adapt-static>>
  +{abstract} nome()
}
class PagamentoPix <<application>> {
  #executarCobranca(f, valor)
  +nome()
}
class PagamentoBoleto <<application>> {
  #validar(f, valor)
  #executarCobranca(f, valor)
  +nome()
}
class PagamentoCartao <<application>> {
  #executarCobranca(f, valor)
  +nome()
}
MeioPagamento <|-- PagamentoPix
MeioPagamento <|-- PagamentoBoleto
MeioPagamento <|-- PagamentoCartao
@enduml
```

## 3. Hot Spot 3 — `Evento` (Template Method)

```plantuml
@startuml
abstract class Evento <<framework>> {
  +organizar(turma) <<fixed>>
  #reservarLocal()
  #contarParticipantes(turma)
  #{abstract} calcularCusto(turma) <<adapt-static>>
  #{abstract} validarParticipantes(turma) <<adapt-static>>
  #{abstract} montarProgramacao(crono, turma) <<adapt-static>>
}
class ColacaoGrau <<application>>
class BaileFormatura <<application>>
class MissaFormatura <<application>>
Evento <|-- ColacaoGrau
Evento <|-- BaileFormatura
Evento <|-- MissaFormatura
@enduml
```

## 4. Hot Spot 1 e 4 — `EstrategiaRateio` / `RegraVotacao` (Strategy / caixa-preta)

```plantuml
@startuml
interface EstrategiaRateio <<framework>> {
  +calcular(turma, custoTotal) <<adapt-static>>
  +nome()
}
class RateioIgualitario <<application>>
class RateioPorAdesao <<application>>
class RateioProporcionalRenda <<application>>
EstrategiaRateio <|.. RateioIgualitario
EstrategiaRateio <|.. RateioPorAdesao
EstrategiaRateio <|.. RateioProporcionalRenda

interface RegraVotacao <<framework>> {
  +apurar(votos, totalAptos) <<adapt-static>>
  +nome()
}
class MaioriaSimples <<application>>
class QuorumQualificado <<application>>
class VotoPonderado <<application>>
RegraVotacao <|.. MaioriaSimples
RegraVotacao <|.. QuorumQualificado
RegraVotacao <|.. VotoPonderado
@enduml
```

## 5. Parte invariante — `GestorFormatura` (caixa-preta / composição)

```plantuml
@startuml
class GestorFormatura <<framework>> {
  -turma : Turma
  -estrategiaRateio : EstrategiaRateio
  -regraVotacao : RegraVotacao
  -canais : List<CanalNotificacao>
  +ratear(custoTotal) <<fixed>>
  +receberPagamento(f, valor, meio) <<fixed>>
  +organizarEvento(evento) <<fixed>>
  +deliberar(votos) <<fixed>>
  +comunicar(mensagem) <<fixed>>
}
interface EstrategiaRateio <<framework>>
interface RegraVotacao <<framework>>
abstract class CanalNotificacao <<framework>>
abstract class MeioPagamento <<framework>>
abstract class Evento <<framework>>

GestorFormatura o--> EstrategiaRateio
GestorFormatura o--> RegraVotacao
GestorFormatura o--> CanalNotificacao
GestorFormatura ..> MeioPagamento
GestorFormatura ..> Evento
note right of GestorFormatura
  Inversão de controle:
  o framework chama as
  implementações plugadas
  pela aplicação.
end note
@enduml
```
