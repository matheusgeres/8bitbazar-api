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
| Java | 21 | Linguagem principal |
| Spring Boot | 3.2.x | Framework web |
| Spring Authorization Server | - | OAuth2/OIDC (self-hosted) |
| MySQL | 8.0 | Banco de dados |
| Liquibase | 4.25 | Migrations de banco |
| Elasticsearch | 8.11 | Busca avançada |
| MinIO | - | Storage de imagens (S3-compatible) |
| RabbitMQ | 3.x | Mensageria assíncrona |
| Gradle | - | Build tool |

## 🏗️ Arquitetura

O projeto segue a **Arquitetura Hexagonal** (Ports & Adapters):

```
src/main/java/com/eightbitbazar/
├── domain/              # Entidades e regras de negócio
├── application/
│   ├── port/
│   │   ├── in/          # Interfaces dos Use Cases
│   │   └── out/         # Interfaces dos Repositories
│   └── usecase/         # Implementação dos Use Cases
└── adapter/
    ├── in/web/          # Controllers (REST API)
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

- Java 21+
- Podman ou Docker
- Gradle 8.x

### 1. Subir a infraestrutura

```bash
podman compose up -d
```

Isso irá iniciar:
- MySQL (porta 3306)
- Elasticsearch (porta 9200)
- MinIO (portas 9000/9001)
- RabbitMQ (portas 5672/15672)
- Liquibase (executa migrations e encerra)

### 2. Executar a aplicação

```bash
./gradlew bootRun
```

A API estará disponível em `http://localhost:8080`

### 3. Acessar serviços auxiliares

- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Elasticsearch**: http://localhost:9200

## 📡 API Endpoints

### OAuth2 (Authorization Server)
- `POST /oauth2/token` - Obter access token
- `POST /oauth2/revoke` - Revogar token
- `GET /oauth2/authorize` - Authorization endpoint
- `GET /.well-known/openid-configuration` - OIDC discovery

### Autenticação
- `POST /auth/register` - Cadastro de usuário
- `POST /auth/forgot-password` - Recuperação de senha (em desenvolvimento)

### Usuário
- `GET /users/me` - Dados do usuário logado
- `PUT /users/me` - Atualizar perfil
- `DELETE /users/me` - Excluir conta

### Listings (Anúncios)
- `POST /listings` - Criar anúncio
- `GET /listings` - Listar com filtros
- `GET /listings/{id}` - Detalhes do anúncio
- `PATCH /listings/{id}` - Atualizar anúncio
- `PATCH /listings/{id}/price` - Atualizar preço (promoção)
- `DELETE /listings/{id}` - Excluir anúncio
- `POST /listings/{id}/images` - Upload de imagens

### Lances
- `POST /listings/{id}/bids` - Dar lance
- `GET /listings/{id}/bids` - Histórico de lances

### Compras
- `POST /listings/{id}/purchase` - Compra direta
- `GET /users/me/purchases` - Minhas compras
- `GET /users/me/sales` - Minhas vendas

### Admin
- `POST /admin/platforms` - Criar plataforma
- `GET /admin/platforms` - Listar plataformas
- `POST /admin/manufacturers` - Criar fabricante
- `GET /admin/manufacturers` - Listar fabricantes

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

### Obter Token (Password Grant)
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -d "grant_type=password" \
  -d "username=seu@email.com" \
  -d "password=suasenha" \
  -d "client_id=eightbitbazar-web"
```

### Usar Token
```
Authorization: Bearer <access-token>
```

### Refresh Token
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -d "grant_type=refresh_token" \
  -d "refresh_token=<refresh-token>" \
  -d "client_id=eightbitbazar-web"
```

## 🧪 Testes

```bash
./gradlew test
```

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor, leia as diretrizes de contribuição antes de enviar um PR.

---

Feito com ❤️ para a comunidade retrogamer
