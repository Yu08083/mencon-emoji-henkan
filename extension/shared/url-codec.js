
import { DEFAULT_CIPHER } from './cipher.js';

const VERSION = 1;

export function buildShareUrl(cipher, baseUrl = 'https://emojicode.github.io/emoji-code-pro/') {
  const diff = {};
  const extra = {};
  for (const [k, v] of Object.entries(cipher)) {
    if (k in DEFAULT_CIPHER) {
      if (DEFAULT_CIPHER[k] !== v) diff[k] = v;
    } else {
      extra[k] = v;
    }
  }
  const payload = { v: VERSION, c: diff, x: extra };
  const encoded = base64UrlEncode(JSON.stringify(payload));
  return `${baseUrl}#cipher=${encoded}`;
}

export function parseShareUrl(text) {
  if (!text) return null;
  const m = String(text).match(/[#?&]cipher=([^&\s]+)/);
  if (!m) return null;
  try {
    const json = base64UrlDecode(m[1]);
    const payload = JSON.parse(json);
    if (payload.v !== VERSION) return null;
    return { ...DEFAULT_CIPHER, ...(payload.c || {}), ...(payload.x || {}) };
  } catch {
    return null;
  }
}

function base64UrlEncode(str) {
  const bytes = new TextEncoder().encode(str);
  let bin = '';
  for (const b of bytes) bin += String.fromCharCode(b);
  return btoa(bin).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function base64UrlDecode(str) {
  const pad = str.length % 4 === 0 ? '' : '='.repeat(4 - (str.length % 4));
  const b64 = (str.replace(/-/g, '+').replace(/_/g, '/')) + pad;
  const bin = atob(b64);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  return new TextDecoder('utf-8').decode(bytes);
}
