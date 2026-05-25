import { $, el, clear } from '../utils/dom.js';
import { escapeHtml } from '../utils/escape.js';
import {
  getFavorites,
  addFavorite,
  deleteFavorite as removeFav,
  loadFavorite,
} from '../storage/favorites-store.js';
import { DEFAULT_CIPHER } from '../data/default-cipher.js';
import { saveCipher } from '../storage/cipher-store.js';

let stateRef = null;
let onChangeCallback = null;

export function initFavorites(state, onChange) {
  stateRef = state;
  onChangeCallback = onChange;
  $('#fav-save-btn')?.addEventListener('click', save);
  $('#fav-name-input')?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') save();
  });
  render();
}

function save() {
  const input = $('#fav-name-input');
  const name = input.value.trim();
  if (!name) { input.focus(); return; }

  const existing = getFavorites().find((f) => f.name === name);
  if (existing && !confirm(`「${name}」を上書きしますか？`)) return;

  addFavorite(name, stateRef.cipher);
  input.value = '';
  render();
}

function render() {
  const list = $('#favorites-list');
  if (!list) return;
  clear(list);

  const favorites = getFavorites();
  if (favorites.length === 0) {
    list.appendChild(el('div', { class: 'fav-empty', text: '保存済みの設定はありません' }));
    return;
  }

  favorites.forEach((fav, i) => {
    const item = el('div', { class: 'fav-item' });
    item.innerHTML = `<span class="fav-name">${escapeHtml(fav.name)}</span>`;
    const loadBtn = el('button', { class: 'ctrl-btn fav-load-btn', type: 'button', text: '読み込む' });
    const delBtn = el('button', { class: 'ctrl-btn fav-del-btn', type: 'button', text: '削除' });
    loadBtn.addEventListener('click', () => load(i));
    delBtn.addEventListener('click', () => del(i));
    item.appendChild(loadBtn);
    item.appendChild(delBtn);
    list.appendChild(item);
  });
}

function load(index) {
  const fav = loadFavorite(index);
  if (!fav) return;
  stateRef.cipher = { ...DEFAULT_CIPHER, ...fav.cipher };
  saveCipher(stateRef.cipher);
  onChangeCallback?.();
}

function del(index) {
  const fav = loadFavorite(index);
  if (!fav) return;
  if (!confirm(`「${fav.name}」を削除しますか？`)) return;
  removeFav(index);
  render();
}
