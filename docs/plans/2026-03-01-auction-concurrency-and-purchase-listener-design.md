# Design: Concorrencia no Fechamento de Leilao e Robustez do PurchaseEventListener

Data: 2026-03-01
Status: aprovado

## Objetivo

Corrigir dois gaps de robustez no backend atual:

- evitar fechamento duplicado de leiloes expirados em execucoes concorrentes
- impedir perda silenciosa de mensagens em `purchase.events`

## Escopo

- endurecer a transicao critica do `CloseExpiredAuctionsUseCase`
- ajustar o `PurchaseEventListener` para falhar explicitamente em erro real
- manter a arquitetura atual baseada em MySQL, Spring AMQP e scheduler simples

## Decisao Principal

Foi aprovada a abordagem de endurecimento do que ja existe, sem introduzir Redis, Mongo, lock distribuido, DLQ customizada ou novo estado de dominio como `CLOSING`.

### Leilao expirado

O controle de concorrencia fica no MySQL, por meio de operacao transacional defensiva no repositório. O use case nao deve depender apenas de "buscar e depois salvar", porque isso permite que duas execucoes concorrentes processem o mesmo anuncio.

### Purchase events

O listener deixa de engolir excecoes. Evento desconhecido pode ser ignorado; erro real de parsing ou processamento deve subir, permitindo redelivery/retry do Spring AMQP/RabbitMQ.

## Abordagens Avaliadas

### 1. Recomendada: controle transacional no MySQL + falha explicita no listener

Prós:

- alinhada ao stack atual
- menor complexidade operacional
- resolve o problema sem nova infraestrutura

Contras:

- exige cuidado no desenho da query de concorrencia

### 2. Lock distribuido + retry no listener

Prós:

- forte para multiplas instancias

Contras:

- complexidade excessiva para o contexto atual

### 3. Estado intermediario + estrategia explicita de DLQ

Prós:

- mais observabilidade e separacao de cenarios

Contras:

- expande dominio e infraestrutura antes da necessidade

## Arquitetura Aprovada

### Fechamento de leilao

O `AuctionClosingScheduler` continua fino e apenas dispara o use case.

O `CloseExpiredAuctionsUseCase` continua sendo o centro da regra, mas passa a operar com apoio de um contrato de repositório com semantica de concorrencia forte. A regra de negocio continua no use case; a defesa contra corrida fica encapsulada no acesso ao banco.

O fluxo aprovado e:

1. scheduler dispara o use case
2. use case busca um lote pequeno de anuncios elegiveis
3. antes de fechar cada anuncio, o repositório aplica lock ou claim transacional
4. apenas o worker que obtiver o controle continua
5. esse worker conclui a compra, atualiza o anuncio e publica eventos
6. execucoes concorrentes subsequentes devem falhar no claim ou encontrar o anuncio fora do criterio

### Listener de compra

O `PurchaseEventListener` continua pequeno e idempotente.

O fluxo aprovado e:

1. listener recebe a mensagem
2. valida o `eventType`
3. eventos irrelevantes sao ignorados de forma explicita
4. `purchase.completed` e desserializado e processado
5. em sucesso, a mensagem segue o fluxo normal de confirmacao
6. em falha real, a excecao e propagada para permitir redelivery

## Componentes Afetados

- `CloseExpiredAuctionsUseCase`
- `ListingRepository`
- adapter/repositório JPA de `Listing`
- `AuctionClosingScheduler`
- `PurchaseEventListener`
- configuracao AMQP, se necessario para explicitar politica de ack/requeue

## Regras de Erro e Concorrencia

### Leilao

- nao pode haver duas `Purchase` para o mesmo encerramento de leilao
- o anuncio precisa terminar em estado unico e consistente
- a protecao deve ocorrer na transicao critica, nao apenas na observacao do estado final

### Listener

- evento desconhecido nao quebra o fluxo
- erro de processamento nao pode ser reduzido a log
- o sistema deve preferir retry a perda silenciosa

## Testes Necessarios

- teste unitario do use case cobrindo falha de claim/lock
- teste de integracao de reexecucao/concorrencia controlada para leilao
- teste garantindo no maximo uma `Purchase` por leilao encerrado
- teste do listener validando sucesso em `purchase.completed`
- teste do listener validando propagacao de excecao em falha real
- teste de integracao observando redelivery ou ao menos falha nao silenciosa no pipeline de AMQP

## Criterios de Sucesso

- o fechamento de leilao deixa de assumir instância unica como premissa oculta
- `purchase.completed` deixa de ser descartado silenciosamente
- nenhuma infraestrutura nova e adicionada
- o comportamento permanece alinhado ao restante da arquitetura atual

## Observacao Sobre Escopo

O design deliberadamente nao introduz:

- Redis
- lock distribuido
- MongoDB
- estado intermediario de dominio
- fila nova de dead-letter customizada

Esses pontos podem ser revisitados no futuro, mas nao sao necessarios para a correcao atual.
