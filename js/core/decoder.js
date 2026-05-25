import { toGraphemes } from './kana-utils.js';
import { DAKUTEN_MAP, HANDAKUTEN_MAP, DAKUTEN_MARKS, HANDAKUTEN_MARKS } from '../data/kana-maps.js';

export function decode(input, cipher) {
  if (!input) return '';


  const reverse = {};
  for (const [kana, emoji] of Object.entries(cipher)) {
    reverse[emoji] = kana;
  }


  const dakutenReverse = {};
  for (const [d, base] of Object.entries(DAKUTEN_MAP)) dakutenReverse[base] = d;
  const handakutenReverse = {};
  for (const [h, base] of Object.entries(HANDAKUTEN_MAP)) handakutenReverse[base] = h;

  const segments = toGraphemes(input);
  let result = '';
  let i = 0;

  while (i < segments.length) {
    const seg = segments[i];
    const next = segments[i + 1];

    if (reverse[seg]) {
      const kana = reverse[seg];
      if (next && DAKUTEN_MARKS.has(next) && dakutenReverse[kana]) {
        result += dakutenReverse[kana];
        i += 2;
        continue;
      }
      if (next && HANDAKUTEN_MARKS.has(next) && handakutenReverse[kana]) {
        result += handakutenReverse[kana];
        i += 2;
        continue;
      }
      result += kana;
    } else {

      result += seg;
    }
    i++;
  }

  return result;
}

export function containsCipherEmoji(input, cipher) {
  if (!input) return false;
  for (const emoji of Object.values(cipher)) {
    if (input.includes(emoji)) return true;
  }
  return false;
}
