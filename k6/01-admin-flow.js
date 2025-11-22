import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, defaultHeaders, authHeaders, defaultThresholds } from './config.js';

export const options = {
  vus: 2,
  duration: '1m',
  thresholds: defaultThresholds,
};

// Helper para login
function login(email, password) {
  const res = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ email, password }),
    { headers: defaultHeaders }
  );

  if (res.status === 200) {
    return res.json().accessToken;
  }
  return null;
}

// Helper para registrar usuário
function register(email, password, name) {
  const res = http.post(
    `${BASE_URL}/api/v1/auth/register`,
    JSON.stringify({ email, password, name }),
    { headers: defaultHeaders }
  );

  return res.status === 201;
}

export function setup() {
  // Usar o admin que a aplicação gera
  const admin = {
    email: 'admin@8bitbazar.com',
    password: 'Admin@123',
  };

  const token = login(admin.email, admin.password);

  if (!token) {
    console.error('Admin login failed');
    return { token: null };
  }

  const timestamp = Date.now();

  // Criar fabricante uma única vez
  const manufacturerRes = http.post(
    `${BASE_URL}/api/v1/admin/manufacturers`,
    JSON.stringify({
      name: `Test Manufacturer ${timestamp}`,
      country: 'Japan',
      foundedYear: 1985,
    }),
    { headers: authHeaders(token) }
  );

  let manufacturerId = 1;
  if (manufacturerRes.status === 201) {
    manufacturerId = manufacturerRes.json().id;
    console.log(`Created manufacturer: ${manufacturerId}`);
  }

  // Criar plataforma uma única vez
  const platformRes = http.post(
    `${BASE_URL}/api/v1/admin/platforms`,
    JSON.stringify({
      name: `Test Platform ${timestamp}`,
      abbreviation: 'TST',
      generation: 4,
      releaseYear: 1990,
      manufacturerId: manufacturerId,
    }),
    { headers: authHeaders(token) }
  );

  let platformId = 1;
  if (platformRes.status === 201) {
    platformId = platformRes.json().id;
    console.log(`Created platform: ${platformId}`);
  }

  return { admin, token, manufacturerId, platformId };
}

export default function (data) {
  const { token } = data;

  if (!token) {
    console.error('No token available');
    return;
  }

  // 1. Listar fabricantes (List Manufacturers)
  const listManufacturersRes = http.get(
    `${BASE_URL}/api/v1/admin/manufacturers`,
    { headers: authHeaders(token) }
  );

  check(listManufacturersRes, {
    'list manufacturers status is 200': (r) => r.status === 200,
    'manufacturers is array': (r) => {
      const body = r.json();
      return Array.isArray(body) || (body && body.content);
    },
  });

  sleep(1);

  // 2. Listar plataformas (List Platforms)
  const listPlatformsRes = http.get(
    `${BASE_URL}/api/v1/admin/platforms`,
    { headers: authHeaders(token) }
  );

  check(listPlatformsRes, {
    'list platforms status is 200': (r) => r.status === 200,
    'platforms is array': (r) => {
      const body = r.json();
      return Array.isArray(body) || (body && body.content);
    },
  });

  sleep(2);
}
