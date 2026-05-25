import { $, el, clear } from '../utils/dom.js';
import { DEFAULT_CIPHER, KANA_ORDER } from '../data/default-cipher.js';
import { saveCipher, hasCustomCipher } from '../storage/cipher-store.js';

let editMode = false;
let stateRef = null;
let onChangeCallback = null;

export function initCipherTable(state, onChange) {
  stateRef = state;
  onChangeCallback = onChange;
  $('#table-header')?.addEventListener('click', toggleTable);
  $('#edit-btn')?.addEventListener('click', toggleEditMode);
  $('#reset-btn')?.addEventListener('click', resetCipherUI);
  refreshResetBtn();
  rebuildGrid();
}

function toggleTable() {
  const body = $('#table-body');
  const icon = $('#toggle-icon');
  if (!body) return;
  const open = body.style.display === 'none';
  body.style.display = open ? '' : 'none';
  icon?.classList.toggle('open', open);
}

function toggleEditMode() {
  editMode = !editMode;
  const btn = $('#edit-btn');
  if (btn) {
    btn.textContent = editMode ? '完了' : 'カスタマイズ';
    btn.classList.toggle('active-edit', editMode);
  }
  rebuildGrid();
}

function resetCipherUI() {
  if (!confirm('デフォルトの暗号表に戻しますか？')) return;
  stateRef.cipher = { ...DEFAULT_CIPHER };
  saveCipher(stateRef.cipher);
  refreshResetBtn();
  rebuildGrid();
  onChangeCallback?.();
}

function refreshResetBtn() {
  const btn = $('#reset-btn');
  if (!btn) return;
  btn.style.display = hasCustomCipher() ? '' : 'none';
}

export function rebuildGrid() {
  const grid = $('#cipher-grid');
  if (!grid) return;
  clear(grid);

  for (const kana of KANA_ORDER) {
    if (!kana) {

      grid.appendChild(el('div', { class: 'cipher-cell empty' }));
      continue;
    }
    const emoji = stateRef.cipher[kana] ?? '';
    const cell = el('div', { class: 'cipher-cell' + (editMode ? ' editing' : '') });
    cell.appendChild(el('span', { class: 'cipher-kana', text: kana }));

    if (editMode) {
      const input = el('input', {
        class: 'emoji-input',
        type: 'text',
        value: emoji,
        'aria-label': `${kana}の絵文字`,
      });
      input.addEventListener('change', (e) => {
        const val = e.target.value.trim();
        if (val) {
          stateRef.cipher[kana] = val;
          saveCipher(stateRef.cipher);
          refreshResetBtn();
          onChangeCallback?.();
        } else {
          e.target.value = stateRef.cipher[kana];
        }
      });
      cell.appendChild(input);
    } else {
      cell.appendChild(el('span', { class: 'cipher-emoji', text: emoji }));
    }
    grid.appendChild(cell);
  }
}
