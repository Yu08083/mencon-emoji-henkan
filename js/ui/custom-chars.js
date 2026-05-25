import { $, el, clear } from '../utils/dom.js';
import { DEFAULT_CIPHER } from '../data/default-cipher.js';
import { saveCipher } from '../storage/cipher-store.js';

let stateRef = null;
let onChangeCallback = null;

export function initCustomChars(state, onChange) {
  stateRef = state;
  onChangeCallback = onChange;
  $('#add-custom-btn')?.addEventListener('click', () => render(true));
  render();
}

function render(showAdder = false) {
  const list = $('#custom-chars-list');
  if (!list) return;
  clear(list);

  const entries = Object.entries(stateRef.cipher).filter(([k]) => !(k in DEFAULT_CIPHER));

  for (const [char, emoji] of entries) {
    const row = el('div', { class: 'custom-char-row' }, [
      el('span', { class: 'custom-char-value', text: char }),
      el('span', { class: 'custom-char-arrow', text: '→' }),
      el('span', { class: 'custom-char-emoji', text: emoji }),
    ]);
    const delBtn = el('button', {
      class: 'ctrl-btn fav-del-btn',
      text: '削除',
      type: 'button',
    });
    delBtn.addEventListener('click', () => {
      delete stateRef.cipher[char];
      saveCipher(stateRef.cipher);
      render();
      onChangeCallback?.();
    });
    row.appendChild(delBtn);
    list.appendChild(row);
  }

  if (entries.length === 0 && !showAdder) {
    list.appendChild(el('div', { class: 'fav-empty', text: '特殊文字の変換はまだありません' }));
  }

  if (showAdder) {
    list.appendChild(buildAdderRow());
  }
}

function buildAdderRow() {
  const charInput = el('input', {
    type: 'text',
    class: 'custom-char-input',
    placeholder: '文字',
    maxlength: 10,
  });
  const emojiInput = el('input', {
    type: 'text',
    class: 'custom-char-input emoji-wide',
    placeholder: '絵文字',
  });
  const addBtn = el('button', {
    class: 'ctrl-btn fav-load-btn',
    type: 'button',
    text: '決定',
  });

  const confirm = () => {
    const char = charInput.value.trim();
    const emoji = emojiInput.value.trim();
    if (!char || !emoji) return;
    stateRef.cipher[char] = emoji;
    saveCipher(stateRef.cipher);
    render();
    onChangeCallback?.();
  };

  addBtn.addEventListener('click', confirm);
  emojiInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') confirm(); });
  charInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') emojiInput.focus(); });

  const row = el('div', { class: 'custom-char-row new-row' }, [
    charInput,
    el('span', { class: 'custom-char-arrow', text: '→' }),
    emojiInput,
    addBtn,
  ]);
  setTimeout(() => charInput.focus(), 0);
  return row;
}
