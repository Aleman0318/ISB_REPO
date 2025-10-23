// static/JS/isb-user.js
const DB_NAME = 'UsuarioIndexDB';
const DB_VERSION = 1;
const STORE = 'session';

function openDB() {
  return new Promise((resolve, reject) => {
    if (!('indexedDB' in window)) {
      resolve(null); // fallback
      return;
    }
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
  if (!db) {
    localStorage.setItem('isb.currentUser', JSON.stringify(obj));
    return;
  }
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
  if (!db) {
    localStorage.removeItem('isb.currentUser');
    return;
  }
  await new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, 'readwrite');
    tx.objectStore(STORE).delete(key);
    tx.oncomplete = resolve;
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

// ===== API pública =====
export async function saveUser(user) {
  if (!user) return;
  await idbPut({ key: 'currentUser', ...user });
  const span = document.querySelector('[data-usuario-target]');
  if (span) span.textContent = user.nombre ?? user.username ?? '';
}

export async function getUser() {
  const obj = await idbGet('currentUser');
  if (!obj) return null;
  const { nombre, username, id } = obj;
  return { nombre, username, id };
}

export async function clearUser() {
  await idbDelete('currentUser');
  const span = document.querySelector('[data-usuario-target]');
  if (span) span.textContent = '';
}

/**
 * 1) Si el backend dejó el nombre en el DOM (data-usuario-server), usarlo y persistir.
 * 2) Si no, leer desde IndexedDB y mostrar.
 */
export async function hydrateUserDisplay() {
  const host = document.querySelector('[data-usuario-target]');
  const serverValEl = document.querySelector('[data-usuario-server]');
  if (!host) return;

  // 1️⃣ Mostrar inmediatamente el nombre cacheado (si lo hay)
  const cached = localStorage.getItem('lastUserName');
  if (cached) host.textContent = cached;

  // 2️⃣ Luego buscar en IndexedDB o "semilla" del servidor
  const user = await getUser();
  const serverNombre = serverValEl?.textContent?.trim();

  if (serverNombre && serverNombre.toLowerCase() !== 'invitado') {
    await saveUser({ nombre: serverNombre });
    localStorage.setItem('lastUserName', serverNombre);
    host.textContent = serverNombre;
  } else if (user) {
    localStorage.setItem('lastUserName', user.nombre);
    host.textContent = user.nombre;
  } else {
    host.textContent = 'Invitado';
    localStorage.removeItem('lastUserName');
  }
}
