import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, defaultHeaders, authHeaders, defaultThresholds } from './config.js';

export const options = {
  vus: 5,
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
function register(user) {
  const res = http.post(
    `${BASE_URL}/api/v1/auth/register`,
    JSON.stringify(user),
    { headers: defaultHeaders }
  );

  return res.status === 201;
}

export function setup() {
  const timestamp = Date.now();

  // Usar admin para pegar IDs de plataforma/fabricante
  const adminToken = login('admin@8bitbazar.com', 'Admin@123');

  let platformId = 1;
  let manufacturerId = 1;

  if (adminToken) {
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

  // Criar vendedor
  const seller = {
    email: `seller_setup_${timestamp}@test.com`,
    password: 'Seller@123',
    nickname: `sellersetup${timestamp}`,
    fullName: 'Setup Seller',
    phone: '11999999999',
    isSeller: true,
  };
  register(seller);
  const sellerToken = login(seller.email, seller.password);

  if (!sellerToken) {
    console.error('Seller login failed');
    return { listingIds: [], auctionId: null, platformId, manufacturerId };
  }

  // Criar algumas listagens para os compradores
  const listingIds = [];

  for (let i = 0; i < 10; i++) {
    const listingRes = http.post(
      `${BASE_URL}/api/v1/listings`,
      JSON.stringify({
        name: `Game ${i} - ${timestamp}`,
        description: 'Test listing for purchase flow',
        platformId: platformId,
        manufacturerId: manufacturerId,
        condition: 'COMPLETE',
        quantity: 1,
        type: 'DIRECT_SALE',
        price: 100 + i * 10,
        cashDiscountPercent: 10,
      }),
      { headers: authHeaders(sellerToken) }
    );

    if (listingRes.status === 201) {
      listingIds.push(listingRes.json().id);
    }
  }

  // Criar leilão
  const auctionRes = http.post(
    `${BASE_URL}/api/v1/listings`,
    JSON.stringify({
      name: `Auction ${timestamp}`,
      description: 'Test auction',
      platformId: platformId,
      manufacturerId: manufacturerId,
      condition: 'COMPLETE',
      quantity: 1,
      type: 'AUCTION',
      startingPrice: 50.00,
      buyNowPrice: 200.00,
      auctionEndDate: '2025-12-01T23:59:59',
    }),
    { headers: authHeaders(sellerToken) }
  );

  let auctionId = null;
  if (auctionRes.status === 201) {
    auctionId = auctionRes.json().id;
  }

  console.log(`Setup: created ${listingIds.length} listings and auction ${auctionId}`);

  return { listingIds, auctionId, platformId, manufacturerId };
}

export default function (data) {
  const { listingIds, auctionId } = data;
  const timestamp = Date.now();
  const vuId = __VU;

  // 1. Registrar comprador
  const buyer = {
    email: `buyer_${vuId}_${timestamp}@test.com`,
    password: 'Buyer@123',
    nickname: `buyer${vuId}${timestamp}`,
    fullName: `Test Buyer ${vuId}`,
    phone: '11999999999',
    isSeller: false,
  };

  const registerRes = http.post(
    `${BASE_URL}/api/v1/auth/register`,
    JSON.stringify(buyer),
    { headers: defaultHeaders }
  );

  check(registerRes, {
    'buyer register status is 201': (r) => r.status === 201,
  });

  if (registerRes.status !== 201) {
    console.error(`Buyer register failed: ${registerRes.status}`);
    return;
  }

  sleep(0.5);

  // 2. Login do comprador
  const buyerToken = login(buyer.email, buyer.password);

  check(buyerToken, {
    'buyer login successful': (t) => t !== null,
  });

  if (!buyerToken) {
    console.error('Buyer login failed');
    return;
  }

  sleep(0.5);

  // 3. Pesquisar produtos (Search Listings)
  const searchRes = http.get(
    `${BASE_URL}/api/v1/listings?search=Game&page=0&size=10`,
    { headers: authHeaders(buyerToken) }
  );

  check(searchRes, {
    'search status is 200': (r) => r.status === 200,
    'search returns results': (r) => {
      const body = r.json();
      return body && (body.content || Array.isArray(body));
    },
  });

  sleep(1);

  // 4. Obter detalhes do produto (Get Listing)
  if (listingIds && listingIds.length > 0) {
    const randomIndex = Math.floor(Math.random() * listingIds.length);
    const listingId = listingIds[randomIndex];

    const getRes = http.get(
      `${BASE_URL}/api/v1/listings/${listingId}`,
      { headers: authHeaders(buyerToken) }
    );

    check(getRes, {
      'get listing status is 200': (r) => r.status === 200,
      'listing has details': (r) => r.json() && r.json().name,
    });

    sleep(1);

    // 5. Fazer compra direta (Direct Purchase)
    const purchaseRes = http.post(
      `${BASE_URL}/api/v1/listings/${listingId}/purchase`,
      JSON.stringify({
        paymentMethod: 'PIX',
      }),
      { headers: authHeaders(buyerToken) }
    );

    check(purchaseRes, {
      'purchase request processed': (r) =>
        r.status === 200 || r.status === 201 || r.status === 400 || r.status === 409,
    });

    sleep(1);
  }

  // 6. Colocar oferta em leilão (Place Bid)
  if (auctionId) {
    const bidAmount = 60 + Math.random() * 100; // Bid entre 60 e 160

    const bidRes = http.post(
      `${BASE_URL}/api/v1/listings/${auctionId}/bids`,
      JSON.stringify({
        amount: bidAmount,
      }),
      { headers: authHeaders(buyerToken) }
    );

    check(bidRes, {
      'bid request processed': (r) =>
        r.status === 200 || r.status === 201 || r.status === 400 || r.status === 409,
    });
  }

  sleep(2);
}
