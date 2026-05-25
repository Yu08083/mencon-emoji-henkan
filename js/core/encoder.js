import { katakanaToHiragana } from './kana-utils.js';
import { DAKUTEN_MAP, HANDAKUTEN_MAP, SMALL_KANA_MAP } from '../data/kana-maps.js';

export function encode(input, cipher) {
  if (!input) return '';
  const hira = katakanaToHiragana(input);
  let result = '';

  for (const ch of hira) {
    const base = SMALL_KANA_MAP[ch] ?? ch;
    if (cipher[base]) {
      result += cipher[base];
    } else if (DAKUTEN_MAP[base] && cipher[DAKUTEN_MAP[base]]) {
      result += cipher[DAKUTEN_MAP[base]] + '"';
    } else if (HANDAKUTEN_MAP[base] && cipher[HANDAKUTEN_MAP[base]]) {
      result += cipher[HANDAKUTEN_MAP[base]] + "'";
    } else if (ch === '-') {
      result += 'ー';
    } else {

      result += ch;
    }
  }
  return result;
}
