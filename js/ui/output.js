import { $ } from '../utils/dom.js';
import { escapeHtml } from '../utils/escape.js';

export function setOutput(id, text, placeholder) {
  const box = $(id);
  if (!box) return;
  if (text && text.trim()) {
    box.innerHTML = `<span class="output-text">${escapeHtml(text)}</span>`;
  } else {
    box.innerHTML = `<span class="placeholder-text">${escapeHtml(placeholder)}</span>`;
  }
}
