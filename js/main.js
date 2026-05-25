import { loadCipher, saveCipher } from './storage/cipher-store.js';
import { encode } from './core/encoder.js';
import { decode } from './core/decoder.js';
import { initTheme, toggleTheme } from './ui/theme.js';
import { initTabs, getMode } from './ui/tabs.js';
import { setOutput } from './ui/output.js';
import { bindCopyButtons } from './ui/copy.js';
import { initCipherTable, rebuildGrid } from './ui/cipher-table.js';
import { initCustomChars } from './ui/custom-chars.js';
import { initFavorites } from './ui/favorites.js';
import { initShare } from './ui/share.js';
import { initHistory, render as renderHistory } from './ui/history.js';
import { readCipherFromHash } from './share/url-share.js';
import { pushHistory } from './storage/history-store.js';
import { $ } from './utils/dom.js';

const state = {
  cipher: loadCipher(),
};

let historyTimer = null;

function recompute() {
  const mode = getMode();
  if (mode === 'decode') {
    const input = $('#decode-input').value;
    const out = decode(input, state.cipher);
    setOutput('#decode-output', out, 'ここに文字が表示されます');
    scheduleHistory('decode', input, out);
  } else {
    const input = $('#encode-input').value;
    const out = encode(input, state.cipher);
    setOutput('#encode-output', out, 'ここに絵文字が表示されます');
    scheduleHistory('encode', input, out);
  }
}

function scheduleHistory(mode, input, output) {
  clearTimeout(historyTimer);
  historyTimer = setTimeout(() => {
    pushHistory(mode, input, output);
    renderHistory();
  }, 1200);
}

function bootstrap() {
  initTheme();
  bindCopyButtons();


  $('#theme-toggle')?.addEventListener('click', toggleTheme);


  const incoming = readCipherFromHash();
  if (incoming) {
    if (confirm('共有された暗号表を読み込みますか？\n（現在の暗号表は上書きされます）')) {
      state.cipher = incoming;
      saveCipher(state.cipher);
    }
    history.replaceState(null, '', location.pathname + location.search);
  }


  initTabs(() => recompute());


  $('#decode-input')?.addEventListener('input', recompute);
  $('#encode-input')?.addEventListener('input', recompute);


  initCipherTable(state, () => {
    rebuildGrid();
    recompute();
  });
  initCustomChars(state, recompute);
  initFavorites(state, () => {
    rebuildGrid();
    recompute();
  });
  initShare(state);
  initHistory();

  recompute();
}

document.addEventListener('DOMContentLoaded', bootstrap);
