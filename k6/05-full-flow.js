import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { BASE_URL, defaultHeaders, authHeaders, defaultThresholds } from './config.js';

export const options = {
  vus: 5,
  duration: '2m',
  thresholds: defaultThresholds,
};

// Helpers
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

function register(user) {
  return http.post(
    `${BASE_URL}/api/v1/auth/register`,
    JSON.stringify(user),
    { headers: defaultHeaders }
  );
}

function pickFirst(list) {
  if (!list) return null;
  if (Array.isArray(list) && list.length > 0) return list[0];
  if (list.content && list.content.length > 0) return list.content[0];
  return null;
}

export function setup() {
  const timestamp = Date.now();

  // Buscar IDs válidos usando admin
  const adminToken = login('admin@8bitbazar.com', 'Admin@123');
  let platformId = 1;
  let manufacturerId = 1;

  if (adminToken) {
    const platformsRes = http.get(`${BASE_URL}/api/v1/admin/platforms`, {
      headers: authHeaders(adminToken),
    });
    if (platformsRes.status === 200) {
      const platform = pickFirst(platformsRes.json());
      if (platform) platformId = platform.id;
    }

    const manufacturersRes = http.get(`${BASE_URL}/api/v1/admin/manufacturers`, {
      headers: authHeaders(adminToken),
    });
    if (manufacturersRes.status === 200) {
      const manufacturer = pickFirst(manufacturersRes.json());
      if (manufacturer) manufacturerId = manufacturer.id;
    }
  }

  // Criar vendedor para publicar anúncios
  const seller = {
    email: `seller_full_${timestamp}@test.com`,
    password: 'Seller@123',
    nickname: `sellerfull${timestamp}`,
    fullName: 'Full Flow Seller',
    phone: '11999999999',
    isSeller: true,
  };

  const sellerReg = register(seller);
  check(sellerReg, { 'seller created': (r) => r.status === 201 });
  const sellerToken = login(seller.email, seller.password);
  if (!sellerToken) {
    console.error('Seller login failed');
    return { listingIds: [], auctionId: null, platformId, manufacturerId };
  }

  // Criar algumas vendas diretas
  const listingIds = [];
  for (let i = 0; i < 5; i++) {
    const listingRes = http.post(
      `${BASE_URL}/api/v1/listings`,
      JSON.stringify({
        name: `Game ${i} ${timestamp}`,
        description: 'Test game for sale',
        platformId,
        manufacturerId,
        condition: 'COMPLETE',
        quantity: 3,
        type: 'DIRECT_SALE',
        price: 99.99 + i,
        cashDiscountPercent: 10,
      }),
      { headers: authHeaders(sellerToken) }
    );
    if (listingRes.status === 201) {
      listingIds.push(listingRes.json().id);
    } else {
      console.error(`listing ${i} creation failed ${listingRes.status}: ${listingRes.body}`);
    }
    check(listingRes, { 'listing created': (r) => r.status === 201 });
  }

  // Criar leilão
  let auctionId = null;
  const auctionRes = http.post(
    `${BASE_URL}/api/v1/listings`,
    JSON.stringify({
      name: `Auction ${timestamp}`,
      description: 'Test auction',
      platformId,
      manufacturerId,
      condition: 'COMPLETE',
      quantity: 1,
      type: 'AUCTION',
      startingPrice: 50.0,
      buyNowPrice: 200.0,
      auctionEndDate: '2025-12-01T23:59:59',
    }),
    { headers: authHeaders(sellerToken) }
  );
  if (auctionRes.status === 201) {
    auctionId = auctionRes.json().id;
  } else {
    console.error(`auction creation failed ${auctionRes.status}: ${auctionRes.body}`);
  }
  check(auctionRes, { 'auction created': (r) => r.status === 201 });

  console.log(
    `Setup completo: listings=${listingIds.length}, auction=${auctionId}, platform=${platformId}, manufacturer=${manufacturerId}`
  );

  if (listingIds.length === 0 || !auctionId) {
    throw new Error('Setup falhou ao criar listing/auction');
  }

  return { listingIds, auctionId };
}

function pickNumber(value, fallback) {
  if (value === null || value === undefined) return fallback;
  const num = typeof value === 'number' ? value : parseFloat(value);
  return Number.isFinite(num) ? num : fallback;
}

export default function (data) {
  const { listingIds, auctionId } = data;
  const timestamp = Date.now();
  const vuId = __VU;

  // Criar comprador
  const buyer = {
    email: `buyer_full_${vuId}_${timestamp}@test.com`,
    password: 'Buyer@123',
    nickname: `buyerfull${vuId}${timestamp}`,
    fullName: `Full Flow Buyer ${vuId}`,
    phone: '11999999999',
    isSeller: false,
  };

  group('Auth Flow (buyer)', function () {
    const registerRes = register(buyer);
    check(registerRes, {
      'buyer register 201': (r) => r.status === 201,
    });

    sleep(0.5);
  });

  const buyerToken = login(buyer.email, buyer.password);
  check(buyerToken, { 'buyer login ok': (t) => t !== null });
  if (!buyerToken) {
    console.error('Buyer login failed');
    return;
  }

  sleep(1);

  // Fluxo de compra e lance
  group('Purchase Flow', function () {
    const searchRes = http.get(
      `${BASE_URL}/api/v1/listings?search=Game&page=0&size=10`,
      { headers: authHeaders(buyerToken) }
    );
    check(searchRes, { 'search listings 200': (r) => r.status === 200 });

    sleep(0.5);

    if (listingIds && listingIds.length > 0) {
      const idx = Math.floor(Math.random() * listingIds.length);
      const listingId = listingIds[idx];

      const getRes = http.get(`${BASE_URL}/api/v1/listings/${listingId}`, {
        headers: authHeaders(buyerToken),
      });
      check(getRes, { 'get listing 200': (r) => r.status === 200 });

      sleep(0.5);

      const purchaseRes = http.post(
        `${BASE_URL}/api/v1/listings/${listingId}/purchase`,
        JSON.stringify({ paymentMethod: 'PIX' }),
        { headers: authHeaders(buyerToken) }
      );
      check(purchaseRes, {
        'purchase processed': (r) =>
          r.status === 200 || r.status === 201 || r.status === 400 || r.status === 409,
      });
      if (purchaseRes.status >= 400) {
        console.warn(`purchase fail ${purchaseRes.status}: ${purchaseRes.body}`);
      }
    }

    sleep(0.5);

    if (auctionId) {
      // Pegar detalhes para calcular lance válido (mínimo + 1 e abaixo do buyNow se existir)
      const auctionRes = http.get(`${BASE_URL}/api/v1/listings/${auctionId}`, {
        headers: authHeaders(buyerToken),
      });

      if (auctionRes.status !== 200) {
        console.warn(`auction details fail ${auctionRes.status}: ${auctionRes.body}`);
        return;
      }

      const auctionData = auctionRes.json();
      const highestBid = auctionData.recentBids && auctionData.recentBids.length > 0
        ? pickNumber(auctionData.recentBids[0].amount, pickNumber(auctionData.startingPrice, 50))
        : pickNumber(auctionData.startingPrice, 50);

      const buyNow = pickNumber(auctionData.buyNowPrice, null);
      let bidAmount = highestBid + 1 + Math.random() * 5; // mínimo + (1..6)

      if (buyNow && bidAmount >= buyNow) {
        bidAmount = buyNow - 1; // evitar converter em compra imediata
      }

      // duas casas decimais
      bidAmount = Math.max(bidAmount, highestBid + 1);
      bidAmount = Math.round(bidAmount * 100) / 100;

      const bidRes = http.post(
        `${BASE_URL}/api/v1/listings/${auctionId}/bids`,
        JSON.stringify({ amount: bidAmount }),
        { headers: authHeaders(buyerToken) }
      );
      check(bidRes, {
        'bid processed': (r) =>
          r.status === 201 || r.status === 200 || r.status === 400 || r.status === 409,
      });
      if (bidRes.status >= 400) {
        console.warn(`bid fail ${bidRes.status}: ${bidRes.body}`);
      }
    }
  });

  sleep(2);
}
