import { read, write, remove } from './local-storage.js';
import { DEFAULT_CIPHER } from '../data/default-cipher.js';

const KEY = 'cipher';

export function loadCipher() {
  const diff = read(KEY) || {};
  return { ...DEFAULT_CIPHER, ...diff };
}

export function saveCipher(cipher) {
  const diff = {};
  for (const [k, v] of Object.entries(cipher)) {

    if (DEFAULT_CIPHER[k] === v) continue;
    diff[k] = v;
  }
  if (Object.keys(diff).length === 0) {
    remove(KEY);
  } else {
    write(KEY, diff);
  }
}

export function resetCipher() {
  remove(KEY);
}

export function hasCustomCipher() {
  const diff = read(KEY) || {};
  return Object.keys(diff).length > 0;
}
