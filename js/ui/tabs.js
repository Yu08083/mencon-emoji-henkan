import { $, $$ } from '../utils/dom.js';

export function initTabs(onChange) {
  $$('.tab').forEach((tab) => {
    tab.addEventListener('click', () => {
      const mode = tab.dataset.mode;
      setMode(mode);
      onChange?.(mode);
    });
  });
}

export function setMode(mode) {
  $$('.tab').forEach((t) => t.classList.toggle('active', t.dataset.mode === mode));
  const decode = $('#decode-section');
  const encode = $('#encode-section');
  if (decode) decode.style.display = mode === 'decode' ? '' : 'none';
  if (encode) encode.style.display = mode === 'encode' ? '' : 'none';
}

export function getMode() {
  return $('.tab.active')?.dataset.mode || 'decode';
}
