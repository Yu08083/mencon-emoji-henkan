import { read, write } from '../storage/local-storage.js';

const KEY = 'theme';

export function initTheme() {
  const saved = read(KEY);
  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const theme = saved || (systemDark ? 'dark' : 'light');
  applyTheme(theme);
}

function applyTheme(theme) {
  document.documentElement.dataset.theme = theme;
  const btn = document.getElementById('theme-toggle');
  if (btn) {
    btn.textContent = theme === 'dark' ? '☀' : '☾';
    btn.setAttribute('aria-label', theme === 'dark' ? 'ライトモードへ' : 'ダークモードへ');
  }
}

export function toggleTheme() {
  const current = document.documentElement.dataset.theme || 'light';
  const next = current === 'dark' ? 'light' : 'dark';
  applyTheme(next);
  write(KEY, next);
}
