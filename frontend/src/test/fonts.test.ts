import { describe, expect, it } from 'vitest';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

// Visitors' IPs must not go to Google Fonts: both families are served from the app's own assets
const read = (relative: string) => readFileSync(resolve(__dirname, '../..', relative), 'utf8');
const exists = (relative: string) => existsSync(resolve(__dirname, '../..', relative));
const html = read('index.html');
const indexCss = read('src/styles/index.css');

const fontsCss = exists('src/styles/fonts.css') ? read('src/styles/fonts.css') : '';

interface Face {
  family: string;
  weight: string;
  display: string;
  src: string;
  range: string;
}

const faces: Face[] = Array.from(fontsCss.matchAll(/@font-face\s*\{([^}]*)\}/g), ([, body]) => {
  const get = (property: string) => body.match(new RegExp(`${property}: *([^;]+);`))?.[1].trim() ?? '';
  return {
    family: get('font-family').replace(/['"]/g, ''),
    weight: get('font-weight'),
    display: get('font-display'),
    src: body.match(/url\(['"]?([^'")]+)['"]?\)/)?.[1] ?? '',
    range: get('unicode-range'),
  };
});

const facesOf = (family: string) => faces.filter((face) => face.family === family);

describe('index.html', () => {
  it('does not load anything from Google Fonts', () => {
    expect(html).not.toMatch(/fonts\.googleapis|fonts\.gstatic/);
    expect(html).not.toMatch(/rel="preconnect"/);
  });
});

describe('fonts.css', () => {
  it('is imported by index.css before the Tailwind layers', () => {
    const importAt = indexCss.search(/@import\s+['"]\.\/fonts\.css['"]/);
    expect(importAt).toBeGreaterThanOrEqual(0);
    expect(importAt).toBeLessThan(indexCss.indexOf('@tailwind base'));
  });

  it('declares only local WOFF2 files that exist', () => {
    expect(fontsCss).not.toMatch(/https?:\/\//);
    expect(faces.length).toBeGreaterThan(0);
    for (const face of faces) {
      const file = resolve(__dirname, '../styles', face.src);
      expect(existsSync(file), face.src).toBe(true);
      expect(readFileSync(file).subarray(0, 4).toString('latin1'), face.src).toBe('wOF2');
    }
  });

  it('uses font-display swap and a unicode-range on every face', () => {
    for (const face of faces) {
      expect(face.display, face.src).toBe('swap');
      expect(face.range, face.src).toMatch(/^U\+/);
    }
  });

  it('declares Inter as a variable range covering 300-700 in latin and latin-ext', () => {
    const inter = facesOf('Inter');
    expect(inter).toHaveLength(2);
    for (const face of inter) expect(face.weight).toBe('300 700');
  });

  it('declares JetBrains Mono as a variable range covering 400-800 in latin and latin-ext', () => {
    const mono = facesOf('JetBrains Mono');
    expect(mono).toHaveLength(2);
    for (const face of mono) expect(face.weight).toBe('400 800');
  });
});

describe('font licences', () => {
  it('names both families, the SIL Open Font License and the JetBrains Mono source version', () => {
    const licences = exists('src/assets/fonts/LICENSES.md') ? read('src/assets/fonts/LICENSES.md') : '';
    for (const family of ['Inter', 'JetBrains Mono']) expect(licences).toContain(family);
    expect(licences).toContain('SIL Open Font License');
    expect(licences).toMatch(/@fontsource-variable\/jetbrains-mono 5\.\d+\.\d+/);
  });
});
