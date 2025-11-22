// Configurações compartilhadas para os testes k6

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const defaultHeaders = {
  'Content-Type': 'application/json',
};

export function authHeaders(token) {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

// Dados de teste
export const testUsers = {
  admin: {
    email: 'admin@8bitbazar.com',
    password: 'Admin@123',
  },
  seller: {
    email: `seller_${Date.now()}@test.com`,
    password: 'seller123',
    name: 'Test Seller',
  },
  buyer: {
    email: `buyer_${Date.now()}@test.com`,
    password: 'buyer123',
    name: 'Test Buyer',
  },
};

// Thresholds padrão
export const defaultThresholds = {
  http_req_duration: ['p(95)<500'], // 95% das requisições < 500ms
  http_req_failed: ['rate<0.01'],   // < 1% de falhas
};

// Cenários de carga
export const scenarios = {
  smoke: {
    executor: 'constant-vus',
    vus: 1,
    duration: '30s',
  },
  load: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '1m', target: 10 },
      { duration: '3m', target: 10 },
      { duration: '1m', target: 0 },
    ],
  },
  stress: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '2m', target: 50 },
      { duration: '5m', target: 50 },
      { duration: '2m', target: 0 },
    ],
  },
};
