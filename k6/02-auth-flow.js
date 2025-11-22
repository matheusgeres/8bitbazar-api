import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, defaultHeaders, defaultThresholds } from './config.js';

export const options = {
  vus: 5,
  duration: '1m',
  thresholds: defaultThresholds,
};

// Gera dados únicos por VU
function generateUserData() {
  const timestamp = Date.now();
  const vuId = __VU;
  return {
    email: `user_${vuId}_${timestamp}@test.com`,
    password: 'Test@123',
    name: `Test User ${vuId}`,
  };
}

export default function () {
  const user = generateUserData();

  // 1. Register - Criar nova conta
  const registerPayload = JSON.stringify({
    email: user.email,
    password: user.password,
    nickname: `user${__VU}${Date.now()}`,
    fullName: user.name,
    phone: '11999999999',
    isSeller: false,
  });

  const registerRes = http.post(
    `${BASE_URL}/api/v1/auth/register`,
    registerPayload,
    { headers: defaultHeaders }
  );

  const registerSuccess = check(registerRes, {
    'register status is 201': (r) => r.status === 201,
    'register returns user data': (r) => {
      const body = r.json();
      return body && body.email === user.email;
    },
  });

  if (!registerSuccess) {
    console.error(`Register failed: ${registerRes.status} - ${registerRes.body}`);
    return;
  }

  sleep(1);

  // 2. Login - Autenticar-se
  const loginPayload = JSON.stringify({
    email: user.email,
    password: user.password,
  });

  const loginRes = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    loginPayload,
    { headers: defaultHeaders }
  );

  const loginSuccess = check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'login returns token': (r) => {
      const body = r.json();
      return body && body.accessToken;
    },
  });

  if (!loginSuccess) {
    console.error(`Login failed: ${loginRes.status} - ${loginRes.body}`);
    return;
  }

  const token = loginRes.json().accessToken;

  // 3. Get current user - Usar token nas requisições
  const meRes = http.get(`${BASE_URL}/api/v1/users/me`, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  check(meRes, {
    'get me status is 200': (r) => r.status === 200,
    'get me returns correct email': (r) => {
      const body = r.json();
      return body && body.email === user.email;
    },
  });

  sleep(1);
}
