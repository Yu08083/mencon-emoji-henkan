import { $, el } from '../utils/dom.js';
import { buildShareUrl } from '../share/url-share.js';
import { generateQrSvg } from '../share/qr.js';
import { copyText } from './copy.js';

let stateRef = null;

export function initShare(state) {
  stateRef = state;
  $('#share-btn')?.addEventListener('click', openModal);
  $('#share-modal-close')?.addEventListener('click', closeModal);
  $('#share-modal-backdrop')?.addEventListener('click', closeModal);
  $('#share-url-copy')?.addEventListener('click', () => {
    const url = $('#share-url').textContent;
    copyText(url, $('#share-url-copy'));
  });
}

function openModal() {
  const url = buildShareUrl(stateRef.cipher);
  const urlBox = $('#share-url');
  const qrBox = $('#share-qr');
  if (urlBox) urlBox.textContent = url;
  if (qrBox) {
    try {
      qrBox.innerHTML = generateQrSvg(url, { scale: 6, margin: 2 });
    } catch (e) {
      qrBox.innerHTML = '<div class="qr-error">QRコードを生成できませんでした。<br>URLからインポートしてください。</div>';
    }
  }
  $('#share-modal')?.classList.add('open');
}

function closeModal() {
  $('#share-modal')?.classList.remove('open');
}
