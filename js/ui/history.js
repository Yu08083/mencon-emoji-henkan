import { $, el, clear } from '../utils/dom.js';
import { escapeHtml } from '../utils/escape.js';
import { getHistory, clearHistory } from '../storage/history-store.js';

export function initHistory() {
  $('#history-header')?.addEventListener('click', toggle);
  $('#history-clear')?.addEventListener('click', (e) => {
    e.stopPropagation();
    if (!confirm('履歴をすべて消去しますか？')) return;
    clearHistory();
    render();
  });
  render();
}

function toggle() {
  const body = $('#history-body');
  const icon = $('#history-toggle-icon');
  if (!body) return;
  const open = body.style.display === 'none';
  body.style.display = open ? '' : 'none';
  icon?.classList.toggle('open', open);
  if (open) render();
}

export function render() {
  const list = $('#history-list');
  if (!list) return;
  clear(list);
  const items = getHistory();
  if (items.length === 0) {
    list.appendChild(el('div', { class: 'fav-empty', text: '履歴はまだありません' }));
    return;
  }
  items.forEach((h) => {
    const dir = h.mode === 'decode' ? '🔓' : '🔒';
    const item = el('div', { class: 'history-item' });
    item.innerHTML = `
      <div class="history-mode">${dir} ${h.mode === 'decode' ? '復号' : '暗号化'}</div>
      <div class="history-input">${escapeHtml(truncate(h.input))}</div>
      <div class="history-arrow">↓</div>
      <div class="history-output">${escapeHtml(truncate(h.output))}</div>
    `;
    list.appendChild(item);
  });
}

function truncate(s, max = 40) {
  if (!s) return '';
  return s.length > max ? s.slice(0, max) + '…' : s;
}
