import { $ } from '../utils/dom.js';

export function copyText(text, button) {
  if (!text) return;
  const done = () => {
    if (!button) return;
    const prev = button.textContent;
    button.textContent = '✓ コピー完了';
    button.classList.add('copied');
    setTimeout(() => {
      button.textContent = prev;
      button.classList.remove('copied');
    }, 1500);
  };

  if (navigator.clipboard?.writeText) {
    navigator.clipboard.writeText(text).then(done).catch(() => fallback(text, done));
  } else {
    fallback(text, done);
  }
}

function fallback(text, done) {
  const ta = document.createElement('textarea');
  ta.value = text;
  ta.style.position = 'fixed';
  ta.style.opacity = '0';
  document.body.appendChild(ta);
  ta.select();
  try { document.execCommand('copy'); done?.(); } catch {}
  document.body.removeChild(ta);
}

export function bindCopyButtons() {
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-copy-target]');
    if (!btn) return;
    const target = $(btn.dataset.copyTarget);
    if (!target) return;
    const text = target.textContent.trim();
    copyText(text, btn);
  });
}
