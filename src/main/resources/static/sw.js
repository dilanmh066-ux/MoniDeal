// MoniDeal - Service Worker ligero para soporte PWA e instalación en móviles
const CACHE_NAME = 'monideal-v1';
const ASSETS_TO_CACHE = [
  '/manifest.json',
  '/img/icon.svg',
  '/css/style.css?v=2.5'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      return cache.addAll(ASSETS_TO_CACHE).catch(() => {});
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys => {
      return Promise.all(
        keys.map(key => {
          if (key !== CACHE_NAME) return caches.delete(key);
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', event => {
  // Solo aplicar network-first a peticiones estáticas seguras
  if (event.request.method !== 'GET') return;
  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request))
  );
});
