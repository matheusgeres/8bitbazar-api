# 🎮 8BitBazar

Plataforma de leilões e vendas de jogos retrô (e modernos também!). Um marketplace onde vendedores podem anunciar jogos, consoles e acessórios, e compradores podem dar lances ou comprar diretamente.

## 📋 Sobre o Projeto

O 8BitBazar é uma API REST que combina características de e-commerce com funcionalidades de leilão, criando uma plataforma única para a comunidade gamer. Vendedores podem:

- Cadastrar produtos para venda direta, leilão ou apenas vitrine
- Aplicar promoções e descontos para pagamento à vista (PIX/dinheiro)
- Gerenciar estoque e acompanhar vendas

Compradores podem:

- Buscar produtos por nome, plataforma, fabricante
- Dar lances em leilões
- Realizar compras diretas
- Acompanhar leilões em andamento

## 🛠️ Stack Tecnológica

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Java | 25 | Linguagem principal |
| Spring Boot | 4.0.0 | Framework web |
| Spring Authorization Server | - | OAuth2/OIDC (self-hosted) |
| MySQL | 8.4 | Banco de dados |
| Liquibase | 4.25.1 | Migrations de banco |
| Elasticsearch | 9.0.0 | Busca avançada |
| MinIO | - | Storage de imagens (S3-compatible) |
| RabbitMQ | 3.x | Mensageria assíncrona |
| SpringDoc OpenAPI | 3.0.0 | Documentação da API (Swagger) |
| Gradle | 9.2.1 | Build tool |

## 🏗️ Arquitetura

O projeto segue a **Arquitetura Hexagonal** (Ports & Adapters):

```
src/main/java/br/com/eightbitbazar/
├── domain/              # Entidades e regras de negócio
├── application/
│   ├── port/
│   │   ├── in/          # Interfaces dos Use Cases
│   │   └── out/         # Interfaces dos Repositories
│   └── usecase/         # Implementação dos Use Cases
└── adapter/
    ├── in/
    │   ├── web/         # Controllers (REST API)
    │   └── scheduling/  # Schedulers (ex: fechamento de leilões)
    └── out/
        ├── persistence/ # JPA Repositories
        ├── storage/     # MinIO Adapter
        ├── search/      # Elasticsearch Adapter
        └── messaging/   # RabbitMQ Adapter
```

### Princípios

- **Imutabilidade**: Objetos de domínio são imutáveis quando possível
- **Separação de camadas**: Request/Response (web), Input/Output (usecase), Entities (domain)
- **Use Cases focados**: Cada use case representa uma ação de negócio específica
- **Soft Delete**: Deleção lógica em todas as entidades

## 🚀 Como Executar

### Pré-requisitos

- Java 25+
- Podman ou Docker
- Gradle 9.x

### 1. Subir a infraestrutura

```bash
podman compose up -d
```

Isso irá iniciar:
- MySQL (porta 3306)
- Elasticsearch (porta 9200)
- MinIO (portas 9000/9001)
- RabbitMQ (portas 5672/15672)
- Prometheus (porta 9090)
- Grafana (porta 3001)
- Liquibase (executa migrations e encerra)

### 2. Configurar o MinIO

Crie o bucket para armazenamento de imagens:

```bash
# Via CLI
podman exec eightbitbazar-minio mc mb /data/eightbitbazar-images

# Ou acesse o console: http://localhost:9001
# Login: minioadmin / minioadmin
# Crie o bucket: eightbitbazar-images
```

### 3. Executar a aplicação

```bash
./gradlew bootRun
```

A API estará disponível em `http://localhost:8080`

### 3. Acessar serviços auxiliares

- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Elasticsearch**: http://localhost:9200

### 4. Monitoramento

- **Grafana**: http://localhost:3001 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Métricas da API**: http://localhost:8080/actuator/prometheus
- **Health Check**: http://localhost:8080/actuator/health

### 5. Documentação da API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

A documentação interativa permite testar os endpoints diretamente no navegador, com suporte a autenticação OAuth2.

## 📡 API Endpoints

### OAuth2 (Authorization Server)
- `POST /oauth2/token` - Obter access token
- `POST /oauth2/revoke` - Revogar token
- `GET /oauth2/authorize` - Authorization endpoint
- `GET /.well-known/openid-configuration` - OIDC discovery

### Autenticação
- `POST /api/v1/auth/register` - Cadastro de usuário
- `POST /api/v1/auth/login` - Login (retorna JWT)
- `POST /api/v1/auth/forgot-password` - Recuperação de senha (em desenvolvimento)
- `POST /api/v1/auth/reset-password` - Resetar senha (em desenvolvimento)

### Usuário
- `GET /api/v1/users/me` - Dados do usuário logado
- `PUT /api/v1/users/me` - Atualizar perfil
- `DELETE /api/v1/users/me` - Excluir conta

### Listings (Anúncios)
- `POST /api/v1/listings` - Criar anúncio
- `GET /api/v1/listings` - Listar com filtros
- `GET /api/v1/listings/{id}` - Detalhes do anúncio
- `PATCH /api/v1/listings/{id}` - Atualizar anúncio *(plano futuro)*
- `PATCH /api/v1/listings/{id}/price` - Atualizar preço/promoção *(plano futuro)*
- `DELETE /api/v1/listings/{id}` - Excluir anúncio
- `POST /api/v1/listings/{id}/images` - Upload de imagens

### Lances
- `POST /api/v1/listings/{id}/bids` - Dar lance
- `GET /api/v1/listings/{id}/bids` - Histórico de lances *(plano futuro)*

### Compras
- `POST /api/v1/listings/{id}/purchase` - Compra direta
- `GET /api/v1/users/me/purchases` - Minhas compras (paginado, ordenado por data decrescente)
- `GET /api/v1/users/me/sales` - Minhas vendas (paginado, ordenado por data decrescente)

### Admin
- `POST /api/v1/admin/platforms` - Criar plataforma
- `GET /api/v1/admin/platforms` - Listar plataformas
- `POST /api/v1/admin/manufacturers` - Criar fabricante
- `GET /api/v1/admin/manufacturers` - Listar fabricantes

## 📦 Modelo de Dados

### Tipos de Anúncio
- **AUCTION**: Leilão com prazo definido
- **DIRECT_SALE**: Venda direta por preço fixo
- **SHOWCASE**: Apenas exibição (sem venda)

### Condição do Item
- **SEALED**: Lacrado
- **COMPLETE**: Completo (jogo, caixa, manual)
- **LOOSE**: Só o cartucho/disco
- **DAMAGED**: Com avarias

### Status do Anúncio
- **ACTIVE**: Disponível
- **SOLD**: Vendido
- **EXPIRED**: Leilão encerrado sem lances
- **DELETED**: Removido pelo vendedor

## 🔐 Autenticação

A API utiliza **Spring Authorization Server** com OAuth2/OIDC para autenticação.

### Obter Token (Login)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "seu@email.com", "password": "suasenha"}'
```

### Usar Token
```
Authorization: Bearer <access-token>
```

## 🧪 Testes

```bash
# Padrão (Docker)
./gradlew test

# Podman rootless (Testcontainers)
./gradlew test -Dspring.profiles.active=local-podman
```

> **Nota:** o scheduler de fechamento de leilões é desabilitado por padrão (`app.auctions.closing.enabled=false`). Para habilitá-lo em ambiente local, defina `app.auctions.closing.enabled=true` em `application.yml` ou via variável de ambiente.

## ✅ CI e proteção da `main`

Implementado para a [issue #19](https://github.com/matheusgeres/8bitbazar-api/issues/19).
O workflow `.github/workflows/ci.yml` executa em pull requests direcionados à `main`
e em pushes para `main`. O único check obrigatório é `build`: usa Java 25,
o wrapper Gradle e `./gradlew clean build --no-daemon --console=plain`, incluindo
toda a suíte de testes e a geração dos artefatos da aplicação.

O runner Ubuntu usa Docker. MySQL, Elasticsearch, MinIO e RabbitMQ são iniciados
pela suíte existente com Testcontainers; não é necessário subir o Compose no CI.
Como MinIO Community passou a ser distribuído somente como código-fonte, a fixture
constrói sua imagem usando `src/test/resources/minio/Dockerfile`, com commit oficial
fixado da versão `RELEASE.2025-09-07T16-13-09Z`. A primeira execução precisa de acesso
à internet para baixar as imagens-base e dependências Go; Docker reutiliza o cache
de camadas nas execuções seguintes.
Os relatórios de teste ficam no artifact `test-reports` por 7 dias, inclusive quando
o build falha. Para reproduzir localmente, use Java 25 e Docker disponível:

```bash
./gradlew clean build --no-daemon --console=plain
```

Fluxo de contribuição:

1. Abra um PR para `main`; o check `build` deve terminar com sucesso.
2. Mantenha a branch atualizada com `main` e resolva todas as conversas do PR.
3. Obtenha 1 approval válido. Novos commits invalidam approvals anteriores.
4. Faça o merge somente depois desses requisitos. Check falhando, pendente ou
   ausente bloqueia o merge normal; bypass administrativo existente não foi alterado.

A proteção clássica da `main` exige `build` com atualização da branch obrigatória
(`strict=true`). Force push e exclusão continuam desabilitados. Não foram adicionadas
exigências de assinatura, histórico linear, segundo approval ou pipeline de deploy.
O nome `build` deve permanecer estável e exclusivo entre workflows, pois está
referenciado na proteção remota da branch.

Após publicar o workflow, valide em um PR real que `build` executa automaticamente,
que falhas bloqueiam o merge e que sucesso, approval válido, branch atualizada e
conversas resolvidas permitem o merge. Essa validação depende do GitHub Actions;
o build local não substitui essa confirmação.

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor, leia as diretrizes de contribuição antes de enviar um PR.

---

Feito com ❤️ para a comunidade retrogamer
