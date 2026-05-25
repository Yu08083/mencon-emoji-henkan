

export const DEFAULT_CIPHER = Object.freeze({
  'あ': '🍨', 'い': '🦑', 'う': '🐴', 'え': '🖼️', 'お': '👹',
  'か': '🦟', 'き': '🌳', 'く': '🐻', 'け': '⚔️', 'こ': '🐨',
  'さ': '🈂️', 'し': '4️⃣', 'す': '🍉', 'せ': '🪭', 'そ': '🛷',
  'た': '🥁', 'ち': '🩸', 'つ': '🌙', 'て': '✋', 'と': '🍅',
  'な': '🍐', 'に': '✌️', 'ぬ': '🧸', 'ね': '🐱', 'の': '🧠',
  'は': '🦷', 'ひ': '🔥', 'ふ': '🚢', 'へ': '🐍', 'ほ': '📕',
  'ま': '😷', 'み': '💧', 'む': '💜', 'め': '👀', 'も': '🍑',
  'や': '🗻', 'ゆ': '🏹', 'よ': '🪀',
  'ら': '🎒', 'り': '🍎', 'る': '🇷🇴', 'れ': '🧱', 'ろ': '6️⃣',
  'わ': '🦎', 'を': '🎵', 'ん': '🆖',
});

export const KANA_ORDER = [
  'あ', 'い', 'う', 'え', 'お',
  'か', 'き', 'く', 'け', 'こ',
  'さ', 'し', 'す', 'せ', 'そ',
  'た', 'ち', 'つ', 'て', 'と',
  'な', 'に', 'ぬ', 'ね', 'の',
  'は', 'ひ', 'ふ', 'へ', 'ほ',
  'ま', 'み', 'む', 'め', 'も',
  'や', '', 'ゆ', '', 'よ',
  'ら', 'り', 'る', 'れ', 'ろ',
  'わ', '', 'を', '', 'ん',
];

export const DAKUTEN_MAP = Object.freeze({
  'が':'か','ぎ':'き','ぐ':'く','げ':'け','ご':'こ',
  'ざ':'さ','じ':'し','ず':'す','ぜ':'せ','ぞ':'そ',
  'だ':'た','ぢ':'ち','づ':'つ','で':'て','ど':'と',
  'ば':'は','び':'ひ','ぶ':'ふ','べ':'へ','ぼ':'ほ',
  'ヴ':'う',
});

export const HANDAKUTEN_MAP = Object.freeze({
  'ぱ':'は','ぴ':'ひ','ぷ':'ふ','ぺ':'へ','ぽ':'ほ',
});

export const SMALL_KANA_MAP = Object.freeze({
  'ぁ':'あ','ぃ':'い','ぅ':'う','ぇ':'え','ぉ':'お',
  'っ':'つ','ゃ':'や','ゅ':'ゆ','ょ':'よ','ゎ':'わ',
});

const DAKUTEN_MARKS = new Set(['"', '\u201C', '\u201D', '゛']);
const HANDAKUTEN_MARKS = new Set(["'", '\u2018', '\u2019', '゜']);

function katakanaToHiragana(s) {
  return s.replace(/[ァ-ヶ]/g, (c) => String.fromCharCode(c.charCodeAt(0) - 0x60));
}

const SEGMENTER = (typeof Intl !== 'undefined' && Intl.Segmenter)
  ? new Intl.Segmenter('ja', { granularity: 'grapheme' })
  : null;

function toGraphemes(str) {
  return SEGMENTER ? [...SEGMENTER.segment(str)].map((s) => s.segment) : [...str];
}

export function encode(input, cipher) {
  if (!input) return '';
  const hira = katakanaToHiragana(input);
  let out = '';
  for (const ch of hira) {
    const base = SMALL_KANA_MAP[ch] ?? ch;
    if (cipher[base]) {
      out += cipher[base];
    } else if (DAKUTEN_MAP[base] && cipher[DAKUTEN_MAP[base]]) {
      out += cipher[DAKUTEN_MAP[base]] + '"';
    } else if (HANDAKUTEN_MAP[base] && cipher[HANDAKUTEN_MAP[base]]) {
      out += cipher[HANDAKUTEN_MAP[base]] + "'";
    } else if (ch === '-') {
      out += 'ー';
    } else {
      out += ch;
    }
  }
  return out;
}

export function decode(input, cipher) {
  if (!input) return '';
  const reverse = {};
  for (const [kana, emoji] of Object.entries(cipher)) reverse[emoji] = kana;
  const dakutenRev = {};
  for (const [d, base] of Object.entries(DAKUTEN_MAP)) dakutenRev[base] = d;
  const handakutenRev = {};
  for (const [h, base] of Object.entries(HANDAKUTEN_MAP)) handakutenRev[base] = h;

  const segments = toGraphemes(input);
  let result = '';
  let i = 0;
  while (i < segments.length) {
    const seg = segments[i];
    const next = segments[i + 1];
    if (reverse[seg]) {
      const kana = reverse[seg];
      if (next && DAKUTEN_MARKS.has(next) && dakutenRev[kana]) {
        result += dakutenRev[kana]; i += 2; continue;
      }
      if (next && HANDAKUTEN_MARKS.has(next) && handakutenRev[kana]) {
        result += handakutenRev[kana]; i += 2; continue;
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
  for (const e of Object.values(cipher)) if (input.includes(e)) return true;
  return false;
}
