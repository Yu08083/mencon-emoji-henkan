

const EXP = new Uint8Array(512);
const LOG = new Uint8Array(256);
(function initGF() {
  let x = 1;
  for (let i = 0; i < 255; i++) {
    EXP[i] = x;
    LOG[x] = i;
    x <<= 1;
    if (x & 0x100) x ^= 0x11d;
  }
  for (let i = 255; i < 512; i++) EXP[i] = EXP[i - 255];
})();

function gfMul(a, b) { return a && b ? EXP[LOG[a] + LOG[b]] : 0; }

function rsGenerator(degree) {
  let poly = [1];
  for (let i = 0; i < degree; i++) {
    const next = new Array(poly.length + 1).fill(0);
    for (let j = 0; j < poly.length; j++) {
      next[j] ^= gfMul(poly[j], 1);
      next[j + 1] ^= gfMul(poly[j], EXP[i]);
    }
    poly = next;
  }
  return poly;
}

function rsEncode(data, ecLen) {
  const gen = rsGenerator(ecLen);
  const res = new Array(data.length + ecLen).fill(0);
  for (let i = 0; i < data.length; i++) res[i] = data[i];
  for (let i = 0; i < data.length; i++) {
    const factor = res[i];
    if (factor === 0) continue;
    for (let j = 0; j < gen.length; j++) {
      res[i + j] ^= gfMul(gen[j], factor);
    }
  }
  return res.slice(data.length);
}

const VERSION_INFO_M = [
  [16, 10, 1, 16, 0, 0],
  [28, 16, 1, 28, 0, 0],
  [44, 26, 1, 44, 0, 0],
  [64, 18, 2, 32, 0, 0],
  [86, 24, 2, 43, 0, 0],
  [108, 16, 4, 27, 0, 0],
  [124, 18, 4, 31, 0, 0],
  [154, 22, 2, 38, 2, 39],
  [182, 22, 3, 36, 2, 37],
  [216, 26, 4, 43, 1, 44],
];

function getSize(version) { return 17 + version * 4; }

function encodeToMatrix(text) {
  const bytes = new TextEncoder().encode(text);


  let version = 0;
  for (let i = 0; i < VERSION_INFO_M.length; i++) {

    const charLenBits = (i + 1) >= 10 ? 16 : 8;
    const totalBits = 4 + charLenBits + bytes.length * 8;
    if (totalBits <= VERSION_INFO_M[i][0] * 8) { version = i + 1; break; }
  }
  if (version === 0) throw new Error('QR: text too long');

  const info = VERSION_INFO_M[version - 1];
  const totalData = info[0];
  const ecPerBlock = info[1];
  const numBlocks = info[2] + info[4];


  const bits = [];
  const pushBits = (v, n) => { for (let i = n - 1; i >= 0; i--) bits.push((v >> i) & 1); };
  pushBits(0b0100, 4);
  pushBits(bytes.length, version >= 10 ? 16 : 8);
  for (const b of bytes) pushBits(b, 8);
  pushBits(0, Math.min(4, totalData * 8 - bits.length));
  while (bits.length % 8 !== 0) bits.push(0);

  const dataCodewords = [];
  for (let i = 0; i < bits.length; i += 8) {
    let b = 0;
    for (let j = 0; j < 8; j++) b = (b << 1) | bits[i + j];
    dataCodewords.push(b);
  }

  const PAD = [0xEC, 0x11];
  let padIdx = 0;
  while (dataCodewords.length < totalData) dataCodewords.push(PAD[padIdx++ & 1]);


  const blocks = [];
  const ecBlocks = [];
  let offset = 0;
  for (let i = 0; i < info[2]; i++) {
    const block = dataCodewords.slice(offset, offset + info[3]);
    offset += info[3];
    blocks.push(block);
    ecBlocks.push(rsEncode(block, ecPerBlock));
  }
  for (let i = 0; i < info[4]; i++) {
    const block = dataCodewords.slice(offset, offset + info[5]);
    offset += info[5];
    blocks.push(block);
    ecBlocks.push(rsEncode(block, ecPerBlock));
  }


  const finalBytes = [];
  const maxDataLen = Math.max(...blocks.map((b) => b.length));
  for (let i = 0; i < maxDataLen; i++) {
    for (const b of blocks) if (i < b.length) finalBytes.push(b[i]);
  }
  for (let i = 0; i < ecPerBlock; i++) {
    for (const b of ecBlocks) finalBytes.push(b[i]);
  }


  const finalBits = [];
  for (const byte of finalBytes) {
    for (let i = 7; i >= 0; i--) finalBits.push((byte >> i) & 1);
  }


  return buildMatrix(version, finalBits);
}

function buildMatrix(version, dataBits) {
  const size = getSize(version);
  const matrix = Array.from({ length: size }, () => new Int8Array(size).fill(-1));


  function placeFinder(r, c) {
    for (let i = -1; i <= 7; i++) {
      for (let j = -1; j <= 7; j++) {
        const rr = r + i, cc = c + j;
        if (rr < 0 || rr >= size || cc < 0 || cc >= size) continue;
        const inOuter = (i === 0 || i === 6 || j === 0 || j === 6) && (i >= 0 && i <= 6 && j >= 0 && j <= 6);
        const inInner = (i >= 2 && i <= 4 && j >= 2 && j <= 4);
        matrix[rr][cc] = (inOuter || inInner) ? 1 : 0;
      }
    }
  }
  placeFinder(0, 0);
  placeFinder(0, size - 7);
  placeFinder(size - 7, 0);


  for (let i = 8; i < size - 8; i++) {
    matrix[6][i] = (i % 2 === 0) ? 1 : 0;
    matrix[i][6] = (i % 2 === 0) ? 1 : 0;
  }


  matrix[size - 8][8] = 1;


  for (let i = 0; i < 9; i++) {
    if (matrix[8][i] === -1) matrix[8][i] = 0;
    if (matrix[i][8] === -1) matrix[i][8] = 0;
  }
  for (let i = size - 8; i < size; i++) {
    matrix[8][i] = 0;
    matrix[i][8] = 0;
  }


  let bitIdx = 0;
  for (let col = size - 1; col > 0; col -= 2) {
    if (col === 6) col--;
    for (let i = 0; i < size; i++) {
      const upward = ((size - 1 - col) >> 1) % 2 === 0;
      const row = upward ? size - 1 - i : i;
      for (let c = 0; c < 2; c++) {
        const cc = col - c;
        if (matrix[row][cc] === -1) {
          let bit = bitIdx < dataBits.length ? dataBits[bitIdx++] : 0;

          if ((row + cc) % 2 === 0) bit ^= 1;
          matrix[row][cc] = bit;
        }
      }
    }
  }


  const formatBits = [1,0,1,0,1,0,0,0,0,0,1,0,0,1,0];

  for (let i = 0; i < 6; i++) matrix[8][i] = formatBits[i];
  matrix[8][7] = formatBits[6];
  matrix[8][8] = formatBits[7];
  matrix[7][8] = formatBits[8];
  for (let i = 9; i < 15; i++) matrix[14 - i][8] = formatBits[i];

  for (let i = 0; i < 7; i++) matrix[size - 1 - i][8] = formatBits[i];
  for (let i = 7; i < 15; i++) matrix[8][size - 15 + i] = formatBits[i];


  for (let r = 0; r < size; r++) for (let c = 0; c < size; c++) if (matrix[r][c] === -1) matrix[r][c] = 0;

  return matrix;
}

export function generateQrSvg(text, options = {}) {
  const { scale = 8, margin = 4, fg = '#111', bg = '#fff' } = options;
  const matrix = encodeToMatrix(text);
  const size = matrix.length;
  const total = (size + margin * 2) * scale;

  let cells = '';
  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (matrix[r][c]) {
        const x = (c + margin) * scale;
        const y = (r + margin) * scale;
        cells += `<rect x="${x}" y="${y}" width="${scale}" height="${scale}"/>`;
      }
    }
  }

  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${total} ${total}" width="${total}" height="${total}" shape-rendering="crispEdges">
<rect width="100%" height="100%" fill="${bg}"/>
<g fill="${fg}">${cells}</g>
</svg>`;
}
