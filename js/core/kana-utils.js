
export function katakanaToHiragana(str) {
  return str.replace(/[ァ-ヶ]/g, (c) =>
    String.fromCharCode(c.charCodeAt(0) - 0x60)
  );
}

const SEGMENTER = (typeof Intl !== 'undefined' && Intl.Segmenter)
  ? new Intl.Segmenter('ja', { granularity: 'grapheme' })
  : null;

export function toGraphemes(str) {
  if (SEGMENTER) {
    return [...SEGMENTER.segment(str)].map((s) => s.segment);
  }

  return [...str];
}
