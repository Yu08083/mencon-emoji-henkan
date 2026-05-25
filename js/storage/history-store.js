import { read, write } from './local-storage.js';

const KEY = 'history';
const MAX = 20;

export function getHistory() {
  return read(KEY) || [];
}

export function pushHistory(mode, input, output) {
  if (!input || !output) return;
  const list = getHistory();

  if (list[0] && list[0].input === input && list[0].mode === mode) return;
  list.unshift({ mode, input, output, at: Date.now() });
  if (list.length > MAX) list.length = MAX;
  write(KEY, list);
}

export function clearHistory() {
  write(KEY, []);
}
