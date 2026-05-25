

import { DEFAULT_CIPHER } from './cipher.js';

const KEY_CIPHER = 'cipher_diff';
const KEY_ENABLED = 'enabled';
const KEY_FAVORITES = 'favorites';

export async function getCipher() {
  const data = await chrome.storage.sync.get(KEY_CIPHER);
  const diff = data[KEY_CIPHER] || {};
  return { ...DEFAULT_CIPHER, ...diff };
}

export async function setCipher(cipher) {
  const diff = {};
  for (const [k, v] of Object.entries(cipher)) {
    if (DEFAULT_CIPHER[k] === v) continue;
    diff[k] = v;
  }
  await chrome.storage.sync.set({ [KEY_CIPHER]: diff });
}

export async function resetCipher() {
  await chrome.storage.sync.remove(KEY_CIPHER);
}

export async function isEnabled() {
  const data = await chrome.storage.sync.get(KEY_ENABLED);
  return data[KEY_ENABLED] !== false;
}

export async function setEnabled(value) {
  await chrome.storage.sync.set({ [KEY_ENABLED]: value });
}

export async function getFavorites() {
  const data = await chrome.storage.sync.get(KEY_FAVORITES);
  return data[KEY_FAVORITES] || [];
}

export async function setFavorites(list) {
  await chrome.storage.sync.set({ [KEY_FAVORITES]: list });
}

export function onCipherChange(callback) {
  chrome.storage.onChanged.addListener(async (changes, area) => {
    if (area !== 'sync') return;
    if (changes[KEY_CIPHER] || changes[KEY_ENABLED]) {
      const cipher = await getCipher();
      const enabled = await isEnabled();
      callback({ cipher, enabled });
    }
  });
}
