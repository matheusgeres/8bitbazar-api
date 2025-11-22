import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { BASE_URL, defaultHeaders, authHeaders, defaultThresholds, scenarios } from './config.js';

// Configurar para não considerar 403 como falha (comportamento esperado para não-admin)
const adminExpectedStatuses = {
  responseCallback: http.expectedStatuses(200, 201, 403),
};

export const options = {
  vus: 5,
  duration: '2m',
  thresholds: defaultThresholds,
};

// Helpers
function generateUser() {
  const timestamp = Date.now();
  const vuId = __VU;
  return {
    email: `user_${vuId}_${timestamp}@test.com`,
    password: 'Test@123',
    nickname: `user${vuId}${timestamp}`,
    fullName: `Test User ${vuId}`,
    phone: '11999999999',
    isSeller: true,
  };
}

function register(user) {
  return http.post(
    `${BASE_URL}/api/v1/auth/register`,
    JSON.stringify(user),
    { headers: defaultHeaders }
  );
}

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

export default function () {
  const user = generateUser();
  let token = null;

  // Auth Flow
  group('Auth Flow', function () {
    // Register
    const registerRes = register(user);
    check(registerRes, {
      'register status is 201': (r) => r.status === 201,
    });

    sleep(0.5);

    // Login
    token = login(user.email, user.password);
    check(token, {
      'login successful': (t) => t !== null,
    });

    if (token) {
      // Get me
      const meRes = http.get(`${BASE_URL}/api/v1/users/me`, {
        headers: authHeaders(token),
      });
      check(meRes, {
        'get me status is 200': (r) => r.status === 200,
      });
    }
  });

  if (!token) {
    console.error('Auth failed, skipping other flows');
    return;
  }

  sleep(1);

  // Admin Flow (criar dados de referência)
  let manufacturerId = 1;
  let platformId = 1;

  group('Admin Flow', function () {
    // Create Manufacturer
    const manufacturerRes = http.post(
      `${BASE_URL}/api/v1/admin/manufacturers`,
      JSON.stringify({
        name: `Manufacturer ${Date.now()}`,
        country: 'Japan',
        foundedYear: 1985,
      }),
      { headers: authHeaders(token), ...adminExpectedStatuses }
    );

    if (manufacturerRes.status === 201) {
      manufacturerId = manufacturerRes.json().id;
    }

    check(manufacturerRes, {
      'create manufacturer': (r) => r.status === 201 || r.status === 403,
    });

    sleep(0.5);

    // Create Platform
    const platformRes = http.post(
      `${BASE_URL}/api/v1/admin/platforms`,
      JSON.stringify({
        name: `Platform ${Date.now()}`,
        abbreviation: 'PLT',
        generation: 4,
        releaseYear: 1990,
        manufacturerId: manufacturerId,
      }),
      { headers: authHeaders(token), ...adminExpectedStatuses }
    );

    if (platformRes.status === 201) {
      platformId = platformRes.json().id;
    }

    check(platformRes, {
      'create platform': (r) => r.status === 201 || r.status === 403,
    });

    sleep(0.5);

    // List Platforms
    const listRes = http.get(`${BASE_URL}/api/v1/admin/platforms`, {
      headers: authHeaders(token),
      ...adminExpectedStatuses,
    });
    check(listRes, {
      'list platforms': (r) => r.status === 200 || r.status === 403,
    });
  });

  sleep(1);

  // Sale Flow
  let listingId = null;
  let auctionId = null;

  group('Sale Flow', function () {
    // Create Listing
    const listingRes = http.post(
      `${BASE_URL}/api/v1/listings`,
      JSON.stringify({
        name: `Game ${Date.now()}`,
        description: 'Test game for sale',
        platformId: platformId,
        manufacturerId: manufacturerId,
        condition: 'COMPLETE',
        quantity: 1,
        type: 'DIRECT_SALE',
        price: 99.99,
        cashDiscountPercent: 10,
      }),
      { headers: authHeaders(token) }
    );

    if (listingRes.status === 201) {
      listingId = listingRes.json().id;
    }

    check(listingRes, {
      'create listing': (r) => r.status === 201,
    });

    sleep(0.5);

    // Create Auction
    const auctionRes = http.post(
      `${BASE_URL}/api/v1/listings`,
      JSON.stringify({
        name: `Auction ${Date.now()}`,
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
      { headers: authHeaders(token) }
    );

    if (auctionRes.status === 201) {
      auctionId = auctionRes.json().id;
    }

    check(auctionRes, {
      'create auction': (r) => r.status === 201,
    });

    sleep(0.5);

    // Get Listing
    if (listingId) {
      const getRes = http.get(`${BASE_URL}/api/v1/listings/${listingId}`, {
        headers: authHeaders(token),
      });
      check(getRes, {
        'get listing': (r) => r.status === 200,
      });
    }
  });

  sleep(1);

  // Purchase Flow
  group('Purchase Flow', function () {
    // Search Listings
    const searchRes = http.get(
      `${BASE_URL}/api/v1/listings?search=Game&page=0&size=10`,
      { headers: authHeaders(token) }
    );
    check(searchRes, {
      'search listings': (r) => r.status === 200,
    });

    sleep(0.5);

    // Place Bid (se tiver leilão)
    if (auctionId) {
      const bidRes = http.post(
        `${BASE_URL}/api/v1/listings/${auctionId}/bids`,
        JSON.stringify({ amount: 75.00 }),
        { headers: authHeaders(token) }
      );
      check(bidRes, {
        'place bid': (r) => r.status === 200 || r.status === 201 || r.status === 400,
      });
    }
  });

  sleep(2);
}
