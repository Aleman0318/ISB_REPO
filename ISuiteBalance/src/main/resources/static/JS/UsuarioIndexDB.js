// static/JS/UsuarioIndexDB.js
const DB_NAME = 'UsuarioIndexDB';
const DB_VERSION = 1;
const STORE = 'session';

// ---- Apertura y manejo de IndexedDB ----
function openDB() {
  return new Promise((resolve, reject) => {
    if (!('indexedDB' in window)) { resolve(null); return; }
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE)) {
        db.createObjectStore(STORE, { keyPath: 'key' });
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function idbPut(obj) {
  const db = await openDB();
  if (!db) { localStorage.setItem('isb.currentUser', JSON.stringify(obj)); return; }
  await new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, 'readwrite');
    tx.objectStore(STORE).put(obj);
    tx.oncomplete = resolve;
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

async function idbGet(key) {
  const db = await openDB();
  if (!db) {
    const raw = localStorage.getItem('isb.currentUser');
    return raw ? JSON.parse(raw) : null;
  }
  const val = await new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, 'readonly');
    const req = tx.objectStore(STORE).get(key);
    req.onsuccess = () => resolve(req.result || null);
    req.onerror = () => reject(req.error);
  });
  db.close();
  return val;
}

async function idbDelete(key) {
  const db = await openDB();
  if (!db) { localStorage.removeItem('isb.currentUser'); return; }
  await new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, 'readwrite');
    tx.objectStore(STORE).delete(key);
    tx.oncomplete = resolve;
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

// ---- API pública ----
export async function saveUser(user) {
  if (!user) return;

  // Mezcla con lo que ya haya guardado para no perder datos previos
  const prev = await getUser();

  const payload = {
    key: 'currentUser',
    nombre: user.nombre ?? prev?.nombre ?? '',
    correo: user.correo ?? prev?.correo ?? null,
    rol:    user.rol    ?? prev?.rol    ?? null
  };

  await idbPut(payload);

  // Actualiza visualmente solo el nombre
  const span = document.querySelector('[data-usuario-target]');
  if (span) span.textContent = payload.nombre || 'Invitado';

  // Guarda en localStorage para evitar parpadeo
  if (payload.nombre) localStorage.setItem('lastUserName', payload.nombre);
}

export async function getUser() {
  const obj = await idbGet('currentUser');
  if (!obj) return null;
  const { nombre, correo, rol } = obj;
  return { nombre, correo, rol };
}

export async function clearUser() {
  await idbDelete('currentUser');
  const span = document.querySelector('[data-usuario-target]');
  if (span) span.textContent = '';
  localStorage.removeItem('lastUserName');
}

/**
 * 1️⃣ Usa los datos ocultos del backend (nombre, correo, rol)
 * 2️⃣ Si no existen, lee desde IndexedDB
 * 3️⃣ (Opcional) Si no hay nada, intenta /api/me
 */
export async function hydrateUserDisplay() {
  const host = document.querySelector('[data-usuario-target]');
  if (!host) return;

  // 1️⃣ Muestra el nombre cacheado rápido
  const cached = localStorage.getItem('lastUserName');
  if (cached) host.textContent = cached;

  // Lee datos del backend ocultos en el HTML
  const serverNombre = document.querySelector('[data-usuario-server]')?.textContent?.trim();
  const serverCorreo = document.querySelector('[data-user-email]')?.textContent?.trim();
  const serverRol    = document.querySelector('[data-user-role]') ?.textContent?.trim();

  // 2️⃣ Si hay datos del backend, los guarda en IndexedDB
  if ((serverNombre && serverNombre.toLowerCase() !== 'invitado') || serverCorreo || serverRol) {
    await saveUser({ nombre: serverNombre, correo: serverCorreo, rol: serverRol });
    return;
  }

  // 3️⃣ Si no hay nada, usa IndexedDB
  const user = await getUser();
  if (user?.nombre) {
    host.textContent = user.nombre;
    localStorage.setItem('lastUserName', user.nombre);
    return;
  }

  // 4️⃣ Último recurso: /api/me
  try {
    const r = await fetch('/api/me', { credentials: 'same-origin', headers: { 'Accept':'application/json' }});
    if (r.ok) {
      const me = await r.json(); // {nombre, correo, rol}
      if (me?.nombre) { await saveUser(me); return; }
    }
  } catch {}

  // 5️⃣ Fallback final
  host.textContent = 'Invitado';
  localStorage.removeItem('lastUserName');
}
