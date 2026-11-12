import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { themePresets } from '@/lib/themePresets';

const html = readFileSync(resolve(__dirname, '../../index.html'), 'utf8');

// Inline scripts only: the module entry point has a src attribute
const inlineScripts = Array.from(html.matchAll(/<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)<\/script>/g), ([, code]) => code);

function runThemeInit() {
  new Function(inlineScripts[0])();
}

describe('theme init script in index.html', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('is a single self-contained inline script placed before the module entry point', () => {
    expect(inlineScripts).toHaveLength(1);
    expect(html.indexOf('<script>')).toBeLessThan(html.indexOf('type="module"'));
    // No static attribute: dark-theme visitors would see the default for a frame
    expect(html).not.toMatch(/<html[^>]*data-theme/);
  });

  it.each(themePresets.map((preset) => preset.name))('applies the saved "%s" theme before React starts', (name) => {
    localStorage.setItem('theme', name);
    runThemeInit();
    expect(document.documentElement.getAttribute('data-theme')).toBe(name);
  });

  it('leaves the document alone when no theme is saved', () => {
    runThemeInit();
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
  });

  it('ignores a saved value that is not a known preset', () => {
    localStorage.setItem('theme', '"><script>alert(1)</script>');
    runThemeInit();
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
  });

  it('does not throw when storage is blocked', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new DOMException('blocked', 'SecurityError');
    });
    expect(() => runThemeInit()).not.toThrow();
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
  });
});
