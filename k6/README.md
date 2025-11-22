# Testes de Carga - k6

Testes de carga automatizados para a API 8BitBazar usando [k6](https://k6.io/).

## Instalação do k6

```bash
# Arch Linux (AUR)
yay -S k6

# Ubuntu/Debian
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# macOS
brew install k6

# Docker
docker pull grafana/k6
```

## Estrutura dos Testes

| Arquivo | Descrição |
|---------|-----------|
| `config.js` | Configurações compartilhadas (URLs, headers, thresholds) |
| `01-admin-flow.js` | Fluxo de administração (criar/listar plataformas e fabricantes) |
| `02-auth-flow.js` | Fluxo de autenticação (register, login, get user) |
| `03-sale-flow.js` | Fluxo de venda (criar listagem, criar leilão, deletar) |
| `04-purchase-flow.js` | Fluxo de compra (buscar, ver detalhes, comprar, dar lance) |
| `05-full-flow.js` | Teste completo com todos os fluxos |

## Executar Testes

### Pré-requisitos

1. A API deve estar rodando em `http://localhost:8080`
2. Os dados de referência (plataformas, fabricantes) devem existir

### Executar testes individuais

```bash
# 1. Fluxo de administração
k6 run k6/01-admin-flow.js

# 2. Fluxo de autenticação
k6 run k6/02-auth-flow.js

# 3. Fluxo de venda
k6 run k6/03-sale-flow.js

# 4. Fluxo de compra
k6 run k6/04-purchase-flow.js

# 5. Teste completo
k6 run k6/05-full-flow.js
```

### Customizar URL base

```bash
k6 run -e BASE_URL=http://localhost:8080 k6/02-auth-flow.js
```

### Executar com mais VUs (usuários virtuais)

```bash
k6 run --vus 10 --duration 30s k6/02-auth-flow.js
```

### Teste de fumaça (smoke test)

```bash
k6 run --vus 1 --duration 10s k6/05-full-flow.js
```

### Teste de carga (load test)

```bash
k6 run --vus 20 --duration 5m k6/05-full-flow.js
```

### Teste de estresse (stress test)

```bash
k6 run --vus 50 --duration 10m k6/05-full-flow.js
```

## Gerar Relatório HTML

```bash
k6 run --out json=results.json k6/05-full-flow.js

# Converter para HTML (requer k6-reporter)
# npm install -g k6-html-reporter
# k6-html-reporter -o report.html results.json
```

## Métricas Importantes

- **http_req_duration**: Tempo de resposta das requisições
- **http_req_failed**: Taxa de falhas
- **http_reqs**: Total de requisições
- **vus**: Usuários virtuais ativos

## Thresholds Configurados

- 95% das requisições devem completar em menos de 500ms
- Taxa de falha deve ser menor que 1%

## Executar com Docker

```bash
docker run --rm -i \
  --network host \
  -v $(pwd)/k6:/scripts \
  grafana/k6 run /scripts/05-full-flow.js
```

## Integração com Grafana

Para visualização em tempo real, configure o k6 para enviar métricas ao InfluxDB:

```bash
k6 run --out influxdb=http://localhost:8086/k6 k6/05-full-flow.js
```

Depois importe o [dashboard do k6](https://grafana.com/grafana/dashboards/2587) no Grafana.
