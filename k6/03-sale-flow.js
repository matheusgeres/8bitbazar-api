import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, defaultHeaders, authHeaders, defaultThresholds } from './config.js';

export const options = {
  vus: 3,
  duration: '2m',
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
  // Usar admin para pegar IDs de plataforma/fabricante existentes
  const adminToken = login('admin@8bitbazar.com', 'Admin@123');

  let platformId = 1;
  let manufacturerId = 1;

  if (adminToken) {
    // Buscar plataformas existentes
    const platformsRes = http.get(`${BASE_URL}/api/v1/admin/platforms`, {
      headers: authHeaders(adminToken),
    });
    if (platformsRes.status === 200) {
      const platforms = platformsRes.json();
      if (Array.isArray(platforms) && platforms.length > 0) {
        platformId = platforms[0].id;
      } else if (platforms.content && platforms.content.length > 0) {
        platformId = platforms.content[0].id;
      }
    }

    // Buscar fabricantes existentes
    const manufacturersRes = http.get(`${BASE_URL}/api/v1/admin/manufacturers`, {
      headers: authHeaders(adminToken),
    });
    if (manufacturersRes.status === 200) {
      const manufacturers = manufacturersRes.json();
      if (Array.isArray(manufacturers) && manufacturers.length > 0) {
        manufacturerId = manufacturers[0].id;
      } else if (manufacturers.content && manufacturers.content.length > 0) {
        manufacturerId = manufacturers.content[0].id;
      }
    }
  }

  return { platformId, manufacturerId };
}

export default function (data) {
  const { platformId, manufacturerId } = data;
  const timestamp = Date.now();
  const vuId = __VU;

  // 1. Registrar vendedor
  const seller = {
    email: `seller_${vuId}_${timestamp}@test.com`,
    password: 'Seller@123',
    nickname: `seller${vuId}${timestamp}`,
    fullName: `Test Seller ${vuId}`,
    phone: '11999999999',
    isSeller: true,
  };

  const registerRes = http.post(
    `${BASE_URL}/api/v1/auth/register`,
    JSON.stringify(seller),
    { headers: defaultHeaders }
  );

  if (registerRes.status !== 201) {
    console.error(`Register failed: ${registerRes.status}`);
    return;
  }

  sleep(0.5);

  // 2. Login do vendedor
  const token = login(seller.email, seller.password);

  if (!token) {
    console.error('Login failed');
    return;
  }

  // 3. Criar listagem (Create Listing) - Venda direta
  const listingPayload = JSON.stringify({
    name: `Super Mario Bros ${timestamp}`,
    description: 'Cartucho original em excelente estado',
    platformId: platformId,
    manufacturerId: manufacturerId,
    condition: 'COMPLETE',
    quantity: 1,
    type: 'DIRECT_SALE',
    price: 150.00,
    cashDiscountPercent: 10,
  });

  const createListingRes = http.post(
    `${BASE_URL}/api/v1/listings`,
    listingPayload,
    { headers: authHeaders(token) }
  );

  const listingCreated = check(createListingRes, {
    'create listing status is 201': (r) => r.status === 201,
    'listing has id': (r) => r.json() && r.json().id,
  });

  if (!listingCreated) {
    console.error(`Create listing failed: ${createListingRes.status} - ${createListingRes.body}`);
    sleep(1);
    return;
  }

  const listingId = createListingRes.json().id;
  sleep(1);

  // 2. Obter detalhes do anúncio (Get Listing)
  const getListingRes = http.get(
    `${BASE_URL}/api/v1/listings/${listingId}`,
    { headers: authHeaders(token) }
  );

  check(getListingRes, {
    'get listing status is 200': (r) => r.status === 200,
    'listing has correct id': (r) => r.json() && r.json().id === listingId,
  });

  sleep(1);

  // 5. Criar listagem de leilão (Create Auction)
  const auctionPayload = JSON.stringify({
    name: `Zelda Auction ${timestamp}`,
    description: 'Raro! Cartucho dourado',
    platformId: platformId,
    manufacturerId: manufacturerId,
    condition: 'COMPLETE',
    quantity: 1,
    type: 'AUCTION',
    startingPrice: 200.00,
    buyNowPrice: 500.00,
    auctionEndDate: '2025-12-01T23:59:59',
  });

  const createAuctionRes = http.post(
    `${BASE_URL}/api/v1/listings`,
    auctionPayload,
    { headers: authHeaders(token) }
  );

  check(createAuctionRes, {
    'create auction status is 201': (r) => r.status === 201,
    'auction has id': (r) => r.json() && r.json().id,
  });

  sleep(1);

  // 4. Deletar anúncio (Delete Listing)
  const deleteRes = http.del(
    `${BASE_URL}/api/v1/listings/${listingId}`,
    null,
    { headers: authHeaders(token) }
  );

  check(deleteRes, {
    'delete listing status is 204 or 200': (r) => r.status === 204 || r.status === 200,
  });

  sleep(2);
}
