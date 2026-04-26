# README Update — Correções Pós-Hardening

**Data:** 2026-04-25
**Escopo:** Correções cirúrgicas no README para refletir o estado real da feature `user-trade-history-auction-closing`.

## Problema

O README contém informações desatualizadas introduzidas pelo PR #18:
- Dois endpoints já implementados ainda aparecem como `*(plano futuro)*`
- A árvore de arquitetura não reflete o novo pacote `scheduling/`
- A seção de testes não orienta usuários Podman rootless
- O comportamento padrão do scheduler (`disabled`) não está documentado

## Mudanças

1. **`API Endpoints > Compras`** — remover `*(plano futuro)*` de `/users/me/purchases` e `/users/me/sales`
2. **`Arquitetura` (árvore)** — adicionar `scheduling/` dentro de `adapter/in/`
3. **`Testes`** — adicionar comando com `-Dspring.profiles.active=local-podman` para Podman rootless
4. **`Testes`** — adicionar nota sobre `app.auctions.closing.enabled=false` (scheduler desligado por padrão)

## Fora de escopo

- Documentação de parâmetros de paginação nos endpoints
- Nova seção de Configuração
- Qualquer outro endpoint ou comportamento não relacionado ao PR #18
