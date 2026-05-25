

(async () => {
  const sharedCipher = await import(chrome.runtime.getURL('shared/cipher.js'));
  const sharedStorage = await import(chrome.runtime.getURL('shared/storage.js'));
  const { decode, encode, containsCipherEmoji } = sharedCipher;
  const { getCipher, isEnabled, onCipherChange } = sharedStorage;


  let currentCipher = await getCipher();
  let enabled = await isEnabled();

  onCipherChange(({ cipher, enabled: e }) => {
    currentCipher = cipher;
    enabled = e;
    if (!enabled) removeAllBanners();
    else scanAll();
  });


  const PROCESSED_ATTR = 'data-emojicode-processed';
  const BANNER_CLASS = 'emojicode-banner';




  function isLikelyMessageContainer(el) {
    if (!el || el.nodeType !== Node.ELEMENT_NODE) return false;
    if (el.closest('input, textarea, [contenteditable]')) return false;
    if (el.classList && el.classList.contains(BANNER_CLASS)) return false;

    if (el.hasAttribute(PROCESSED_ATTR)) return false;
    return true;
  }

  function findMessageBlock(textNode) {
    let node = textNode.parentElement;
    let depth = 0;
    while (node && depth < 6) {

      const text = node.textContent || '';
      if (text.length > 0 && text.length < 2000) {
        const isBlock = ['DIV', 'P', 'SPAN', 'LI'].includes(node.tagName);
        if (isBlock) return node;
      }
      node = node.parentElement;
      depth++;
    }
    return textNode.parentElement;
  }

  function injectBanner(block, decoded, original) {
    if (block.hasAttribute(PROCESSED_ATTR)) return;
    block.setAttribute(PROCESSED_ATTR, '1');


    const next = block.nextElementSibling;
    if (next && next.classList?.contains(BANNER_CLASS)) return;

    const banner = document.createElement('div');
    banner.className = BANNER_CLASS;
    banner.innerHTML = `
      <span class="emojicode-banner-icon" aria-hidden="true">🔓</span>
      <span class="emojicode-banner-text"></span>
      <button class="emojicode-banner-copy" type="button" title="コピー">📋</button>
    `;
    banner.querySelector('.emojicode-banner-text').textContent = decoded;
    banner.querySelector('.emojicode-banner-copy').addEventListener('click', (e) => {
      e.stopPropagation();
      navigator.clipboard?.writeText(decoded);
      const btn = e.currentTarget;
      const prev = btn.textContent;
      btn.textContent = '✓';
      setTimeout(() => { btn.textContent = prev; }, 1200);
    });
    block.insertAdjacentElement('afterend', banner);
  }

  function removeAllBanners() {
    document.querySelectorAll('.' + BANNER_CLASS).forEach((n) => n.remove());
    document.querySelectorAll(`[${PROCESSED_ATTR}]`).forEach((n) => n.removeAttribute(PROCESSED_ATTR));
  }


  function scanText(root) {
    if (!enabled) return;
    const walker = document.createTreeWalker(
      root, NodeFilter.SHOW_TEXT, {
        acceptNode(node) {
          const t = node.nodeValue;
          if (!t || t.length < 2) return NodeFilter.FILTER_REJECT;
          if (!containsCipherEmoji(t, currentCipher)) return NodeFilter.FILTER_REJECT;
          if (!isLikelyMessageContainer(node.parentElement)) return NodeFilter.FILTER_REJECT;
          return NodeFilter.FILTER_ACCEPT;
        }
      }
    );

    const targets = [];
    let n;
    while ((n = walker.nextNode())) targets.push(n);

    for (const textNode of targets) {
      const block = findMessageBlock(textNode);
      if (!block || block.hasAttribute(PROCESSED_ATTR)) continue;

      const fullText = block.textContent || '';
      if (!containsCipherEmoji(fullText, currentCipher)) continue;
      const decoded = decode(fullText, currentCipher);
      if (decoded === fullText) continue;
      injectBanner(block, decoded, fullText);
    }
  }

  function scanAll() {
    scanText(document.body);
  }


  let scanScheduled = false;
  function scheduleScan() {
    if (scanScheduled) return;
    scanScheduled = true;
    requestAnimationFrame(() => {
      scanScheduled = false;
      scanAll();
    });
  }

  const observer = new MutationObserver((mutations) => {
    let dirty = false;
    for (const m of mutations) {
      if (m.type === 'childList' && (m.addedNodes.length > 0 || m.removedNodes.length > 0)) {
        dirty = true; break;
      }
      if (m.type === 'characterData') { dirty = true; break; }
    }
    if (dirty) scheduleScan();
  });
  observer.observe(document.body, {
    childList: true,
    subtree: true,
    characterData: true,
  });



  document.addEventListener('keydown', (e) => {
    if (!enabled) return;
    if (!(e.altKey && (e.key === 'e' || e.key === 'E'))) return;
    const active = document.activeElement;
    if (!active) return;
    let text = null;
    let setter = null;
    if (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA') {
      text = active.value;
      setter = (v) => { active.value = v; };
    } else if (active.isContentEditable) {
      text = active.textContent;
      setter = (v) => { active.textContent = v; };
    }
    if (!text) return;
    const encoded = encode(text, currentCipher);
    if (encoded !== text) {
      setter(encoded);

      active.dispatchEvent(new Event('input', { bubbles: true }));
      e.preventDefault();
    }
  }, true);


  scanAll();

  console.log('[メンコン絵文字] 監視開始');
})();
