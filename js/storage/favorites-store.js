import { read, write } from './local-storage.js';

const KEY = 'favorites';

export function getFavorites() {
  return read(KEY) || [];
}

export function saveFavorites(list) {
  write(KEY, list);
}

export function addFavorite(name, cipher) {
  const list = getFavorites();
  const existing = list.findIndex((f) => f.name === name);
  if (existing >= 0) {
    list[existing].cipher = { ...cipher };
    list[existing].updatedAt = Date.now();
  } else {
    list.push({ name, cipher: { ...cipher }, createdAt: Date.now() });
  }
  saveFavorites(list);
}

export function deleteFavorite(index) {
  const list = getFavorites();
  list.splice(index, 1);
  saveFavorites(list);
}

export function loadFavorite(index) {
  const list = getFavorites();
  return list[index] ?? null;
}
