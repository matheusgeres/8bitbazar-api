# 8BitBazar - Tarefas Pendentes

## Prioridade Alta
- [x] Elasticsearch Adapter - Implementar busca avançada de listings
- [x] RabbitMQ Integration - Eventos assíncronos (fechamento de leilões, notificações)
- [x] Login/Autenticação - Endpoint de login com JWT

### OAuth2 JDBC Persistence
- [x] Implementar JdbcRegisteredClientRepository no SecurityConfig
- [x] Implementar JdbcOAuth2AuthorizationService no SecurityConfig
- [x] Implementar JdbcOAuth2AuthorizationConsentService no SecurityConfig
- [x] Criar changelog Liquibase para popular clients OAuth2 iniciais

## Prioridade Média
- [x] CORS Configuration - Para frontend consumir a API
- [x] Swagger/OpenAPI - Documentação da API
- [ ] Mais testes - Cobertura para outros use cases (PlaceBid, DirectPurchase, CreateListing)

## Prioridade Baixa
- [ ] Validações adicionais - Bean Validation nos DTOs
- [ ] Paginação - Melhorar responses de listagem
- [ ] Cache - Redis para dados frequentes
- [ ] Rate Limiting - Proteção contra abuso

## Funcionalidades Futuras
- [ ] Sistema de avaliações/reviews
- [ ] Chat entre comprador/vendedor
- [ ] Notificações por email
- [ ] Relatórios para sellers
