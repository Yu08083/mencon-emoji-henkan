import { decode, encode, DEFAULT_CIPHER, KANA_ORDER } from './shared/cipher.js';
import {
  getCipher, setCipher, resetCipher,
  isEnabled, setEnabled,
} from './shared/storage.js';
import { buildShareUrl, parseShareUrl } from './shared/url-codec.js';

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => [...document.querySelectorAll(sel)];

let cipher = await getCipher();
let mode = 'decode';

const toggle = $('#toggle-enabled');
toggle.checked = await isEnabled();
toggle.addEventListener('change', async () => {
  await setEnabled(toggle.checked);
  showStatus(toggle.checked ? '✓ 自動変換ON' : '⏸ 自動変換OFF');
});

$$('.popup-tab').forEach((tab) => {
  tab.addEventListener('click', () => {
    mode = tab.dataset.mode;
    $$('.popup-tab').forEach((t) => t.classList.toggle('active', t === tab));
    recompute();
  });
});

const inputEl = $('#input');
const outputEl = $('#output');

function recompute() {
  const text = inputEl.value;
  if (!text) { outputEl.textContent = ''; return; }
  outputEl.textContent = (mode === 'decode')
    ? decode(text, cipher)
    : encode(text, cipher);
}

inputEl.addEventListener('input', recompute);

const copyBtn = $('#copy-btn');
copyBtn.addEventListener('click', async () => {
  const text = outputEl.textContent;
  if (!text) return;
  await navigator.clipboard.writeText(text);
  copyBtn.textContent = '✓';
  copyBtn.classList.add('copied');
  setTimeout(() => {
    copyBtn.textContent = 'コピー';
    copyBtn.classList.remove('copied');
  }, 1200);
});

$('#share-cipher').addEventListener('click', async () => {
  const url = buildShareUrl(cipher);
  await navigator.clipboard.writeText(url);
  showStatus('✓ 共有URLをコピーしました');
});

const editorModal = $('#editor-modal');
$('#open-editor').addEventListener('click', () => {
  buildCipherGrid();
  editorModal.hidden = false;
});
$('#editor-close').addEventListener('click', () => editorModal.hidden = true);
editorModal.addEventListener('click', (e) => {
  if (e.target === editorModal) editorModal.hidden = true;
});

function buildCipherGrid() {
  const grid = $('#cipher-grid');
  grid.innerHTML = '';
  for (const kana of KANA_ORDER) {
    const cell = document.createElement('div');
    if (!kana) {
      cell.className = 'popup-cipher-cell empty';
      grid.appendChild(cell);
      continue;
    }
    cell.className = 'popup-cipher-cell';
    cell.innerHTML = `<div class="kana">${kana}</div><div class="emoji">${escapeHtml(cipher[kana] || '')}</div>`;
    cell.addEventListener('click', () => editCell(cell, kana));
    grid.appendChild(cell);
  }
}

function editCell(cell, kana) {
  const current = cipher[kana] || '';
  const input = document.createElement('input');
  input.type = 'text';
  input.value = current;
  cell.innerHTML = `<div class="kana">${kana}</div>`;
  cell.appendChild(input);
  input.focus();
  input.select();

  const commit = async () => {
    const value = input.value.trim();
    if (value) {
      cipher = { ...cipher, [kana]: value };
      await setCipher(cipher);
      recompute();
    }
    buildCipherGrid();
  };
  input.addEventListener('blur', commit);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') { e.preventDefault(); input.blur(); }
    if (e.key === 'Escape') buildCipherGrid();
  });
}

$('#reset-cipher').addEventListener('click', async () => {
  if (!confirm('デフォルトの暗号表に戻しますか？')) return;
  await resetCipher();
  cipher = await getCipher();
  buildCipherGrid();
  recompute();
  showStatus('✓ デフォルトに戻しました');
});

const importModal = $('#import-modal');
$('#import-url').addEventListener('click', () => {
  $('#import-url-input').value = '';
  importModal.hidden = false;
});
$('#import-close').addEventListener('click', () => importModal.hidden = true);
importModal.addEventListener('click', (e) => {
  if (e.target === importModal) importModal.hidden = true;
});

$('#import-confirm').addEventListener('click', async () => {
  const text = $('#import-url-input').value.trim();
  if (!text) return;
  const parsed = parseShareUrl(text);
  if (!parsed) {
    showStatus('⚠ 有効な共有URLではありません');
    return;
  }
  cipher = parsed;
  await setCipher(cipher);
  recompute();
  importModal.hidden = true;
  showStatus('✓ 暗号表を取り込みました');
});

function showStatus(text) {
  const status = $('#status');
  status.textContent = text;
  status.classList.remove('hidden');
  setTimeout(() => status.classList.add('hidden'), 2200);
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}
