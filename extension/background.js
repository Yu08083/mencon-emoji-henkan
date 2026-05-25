

import { decode, encode, containsCipherEmoji } from './shared/cipher.js';
import { getCipher } from './shared/storage.js';

const MENU_DECODE = 'emojicode-decode';
const MENU_ENCODE = 'emojicode-encode';

chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: MENU_DECODE,
    title: '🔓 選択した暗号を復号',
    contexts: ['selection'],
  });
  chrome.contextMenus.create({
    id: MENU_ENCODE,
    title: '🔒 選択したテキストを暗号化',
    contexts: ['selection'],
  });
});

chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (!info.selectionText || !tab?.id) return;
  const cipher = await getCipher();
  let result = '';
  if (info.menuItemId === MENU_DECODE) {
    result = decode(info.selectionText, cipher);
  } else if (info.menuItemId === MENU_ENCODE) {
    result = encode(info.selectionText, cipher);
  } else {
    return;
  }


  try {
    await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: (text) => {
        navigator.clipboard?.writeText(text);
        const div = document.createElement('div');
        div.textContent = '✓ クリップボードにコピー: ' + text;
        Object.assign(div.style, {
          position: 'fixed', bottom: '24px', left: '50%',
          transform: 'translateX(-50%)', zIndex: '2147483647',
          background: 'rgba(20,30,50,0.92)', color: '#fff',
          padding: '12px 18px', borderRadius: '12px',
          font: '14px system-ui, sans-serif',
          boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
          maxWidth: '90%', wordBreak: 'break-word',
        });
        document.body.appendChild(div);
        setTimeout(() => div.remove(), 3500);
      },
      args: [result],
    });
  } catch (e) {

    console.warn('メンコン絵文字:', e);
  }
});
