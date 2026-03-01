# Auction Concurrency and Purchase Listener Hardening Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Eliminar a janela de concorrencia no fechamento de leiloes expirados e impedir perda silenciosa de mensagens em `purchase.events`.

**Architecture:** O endurecimento sera feito sobre os componentes existentes. O fechamento de leilao continuara no `CloseExpiredAuctionsUseCase`, mas com suporte do repositório para claim/lock transacional no MySQL. O `PurchaseEventListener` deixara de engolir falhas reais, permitindo retry/redelivery do Spring AMQP.

**Tech Stack:** Java 21, Spring Boot 3.2, Spring Data JPA, Spring AMQP, MySQL, RabbitMQ, JUnit 5, MockMvc, Testcontainers

---

### Task 1: Definir contrato de concorrencia para fechamento de leiloes

**Files:**
- Modify: `src/main/java/br/com/eightbitbazar/application/port/out/ListingRepository.java`
- Modify: `src/main/java/br/com/eightbitbazar/adapter/out/persistence/ListingRepositoryAdapter.java`
- Modify: `src/main/java/br/com/eightbitbazar/adapter/out/persistence/repository/JpaListingRepository.java`
- Test: `src/test/java/br/com/eightbitbazar/application/usecase/listing/CloseExpiredAuctionsClaimTest.java`

**Step 1: Write the failing test**

```java
@Test
void shouldSkipAuctionWhenClaimFailsBecauseAnotherWorkerAlreadyOwnsIt() {
    // given eligible auction but repository claim returns empty/false
    // when execute closeExpiredAuctions
    // then no purchase is created and no event is published
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests br.com.eightbitbazar.application.usecase.listing.CloseExpiredAuctionsClaimTest`

Expected: FAIL because the repository contract for claim/lock does not exist yet.

**Step 3: Write minimal implementation**

Introduce a repository-level operation with explicit concurrency semantics, for example:

```java
Optional<Listing> claimExpiredAuction(ListingId listingId, LocalDateTime now);
```

or an equivalent lock-based operation that guarantees only one transaction can proceed with each listing.

**Step 4: Run test to verify it passes**

Run: `./gradlew test --tests br.com.eightbitbazar.application.usecase.listing.CloseExpiredAuctionsClaimTest`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/br/com/eightbitbazar/application/port/out/ListingRepository.java \
  src/main/java/br/com/eightbitbazar/adapter/out/persistence/ListingRepositoryAdapter.java \
  src/main/java/br/com/eightbitbazar/adapter/out/persistence/repository/JpaListingRepository.java \
  src/test/java/br/com/eightbitbazar/application/usecase/listing/CloseExpiredAuctionsClaimTest.java
git commit -m "feat: add concurrency claim for expired auctions"
```

### Task 2: Endurecer o `CloseExpiredAuctionsUseCase`

**Files:**
- Modify: `src/main/java/br/com/eightbitbazar/application/usecase/listing/CloseExpiredAuctions.java`
- Modify: `src/test/java/br/com/eightbitbazar/application/usecase/listing/CloseExpiredAuctionsTest.java`

**Step 1: Write the failing tests**

```java
@Test
void shouldNotCreateDuplicatePurchaseWhenAuctionCannotBeClaimedTwice() {
    // given first claim succeeds and second claim fails
    // when use case runs
    // then exactly one purchase path is executed
}
```

```java
@Test
void shouldOnlyPublishEventsAfterSuccessfulClaim() {
    // given auction selected but not claimed
    // when execute
    // then no auction.ended, purchase.completed or listing.sold are published
}
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests br.com.eightbitbazar.application.usecase.listing.CloseExpiredAuctionsTest`

Expected: FAIL because the use case still processes directly from the initial query result.

**Step 3: Write minimal implementation**

Refactor the use case so the critical path becomes:

1. load eligible auctions
2. attempt claim/lock per listing
3. proceed only when claim succeeds
4. compute winner, persist purchase, update listing, publish events

**Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests br.com.eightbitbazar.application.usecase.listing.CloseExpiredAuctionsTest`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/br/com/eightbitbazar/application/usecase/listing/CloseExpiredAuctions.java \
  src/test/java/br/com/eightbitbazar/application/usecase/listing/CloseExpiredAuctionsTest.java
git commit -m "fix: harden expired auction closing against concurrent execution"
```

### Task 3: Cobrir concorrencia/idempotencia em integracao

**Files:**
- Modify: `src/test/java/br/com/eightbitbazar/adapter/in/web/AuctionClosingIntegrationTest.java`

**Step 1: Write the failing integration test**

```java
@Test
void shouldCreateAtMostOnePurchaseWhenClosingUseCaseRunsTwiceForSameExpiredAuction() {
    // create expired auction with winner
    // execute closeExpiredAuctions twice
    // assert purchase count is 1 and listing is SOLD
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests br.com.eightbitbazar.adapter.in.web.AuctionClosingIntegrationTest`

Expected: FAIL if the flow still allows duplicate processing under repeated execution.

**Step 3: Write minimal implementation**

Adjust repository transaction behavior or use case flow until the integration test proves:

- second execution does not create a second purchase
- listing final state remains stable

**Step 4: Run test to verify it passes**

Run: `./gradlew test --tests br.com.eightbitbazar.adapter.in.web.AuctionClosingIntegrationTest`

Expected: PASS

**Step 5: Commit**

```bash
git add src/test/java/br/com/eightbitbazar/adapter/in/web/AuctionClosingIntegrationTest.java
git commit -m "test: verify auction closing idempotency"
```

### Task 4: Corrigir semantica de falha do `PurchaseEventListener`

**Files:**
- Modify: `src/main/java/br/com/eightbitbazar/adapter/in/messaging/PurchaseEventListener.java`
- Modify: `src/test/java/br/com/eightbitbazar/adapter/in/messaging/PurchaseEventListenerTest.java`

**Step 1: Write the failing test**

```java
@Test
void shouldRethrowWhenPurchaseCompletedCannotBeDeserialized() {
    // given purchase.completed message and jsonMapper failure
    // when listener handles message
    // then exception is propagated
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests br.com.eightbitbazar.adapter.in.messaging.PurchaseEventListenerTest`

Expected: FAIL because the listener currently swallows the exception.

**Step 3: Write minimal implementation**

Change the listener to:

- ignore unknown events explicitly
- process `purchase.completed`
- log and rethrow on real failure

**Step 4: Run test to verify it passes**

Run: `./gradlew test --tests br.com.eightbitbazar.adapter.in.messaging.PurchaseEventListenerTest`

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/br/com/eightbitbazar/adapter/in/messaging/PurchaseEventListener.java \
  src/test/java/br/com/eightbitbazar/adapter/in/messaging/PurchaseEventListenerTest.java
git commit -m "fix: propagate purchase event processing failures"
```

### Task 5: Validar redelivery/retry do fluxo de compra

**Files:**
- Modify: `src/test/java/br/com/eightbitbazar/adapter/in/web/PurchaseEventFlowIntegrationTest.java`
- Modify: `src/main/java/br/com/eightbitbazar/config/RabbitMQConfig.java`
- Modify: `src/main/resources/application.yml`

**Step 1: Write the failing integration test**

```java
@Test
void shouldNotLosePurchaseEventSilentlyWhenListenerFails() {
    // force listener failure for purchase.completed
    // assert failure is visible to the messaging pipeline and event is not treated as success
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests br.com.eightbitbazar.adapter.in.web.PurchaseEventFlowIntegrationTest`

Expected: FAIL because the current pipeline does not expose failure semantics clearly enough.

**Step 3: Write minimal implementation**

Use the smallest configuration change needed to make failure semantics explicit in Spring AMQP. Prefer framework defaults or narrow listener/container settings over introducing custom queues or infrastructure.

**Step 4: Run test to verify it passes**

Run: `./gradlew test --tests br.com.eightbitbazar.adapter.in.web.PurchaseEventFlowIntegrationTest`

Expected: PASS

**Step 5: Commit**

```bash
git add src/test/java/br/com/eightbitbazar/adapter/in/web/PurchaseEventFlowIntegrationTest.java \
  src/main/java/br/com/eightbitbazar/config/RabbitMQConfig.java \
  src/main/resources/application.yml
git commit -m "test: verify purchase event retry semantics"
```

### Task 6: Verificacao final

**Files:**
- Review only: `docs/plans/2026-03-01-auction-concurrency-and-purchase-listener-design.md`
- Review only: `docs/plans/2026-03-01-auction-concurrency-and-purchase-listener-implementation.md`

**Step 1: Run focused verification**

Run: `./gradlew test --tests br.com.eightbitbazar.application.usecase.listing.CloseExpiredAuctionsTest --tests br.com.eightbitbazar.adapter.in.web.AuctionClosingIntegrationTest --tests br.com.eightbitbazar.adapter.in.messaging.PurchaseEventListenerTest --tests br.com.eightbitbazar.adapter.in.web.PurchaseEventFlowIntegrationTest`

Expected: PASS

**Step 2: Run full verification**

Run: `./gradlew test`

Expected: PASS

**Step 3: Commit any final test/doc adjustments if needed**

```bash
git add docs/plans/2026-03-01-auction-concurrency-and-purchase-listener-design.md \
  docs/plans/2026-03-01-auction-concurrency-and-purchase-listener-implementation.md
git commit -m "docs: finalize concurrency and listener hardening plan"
```
