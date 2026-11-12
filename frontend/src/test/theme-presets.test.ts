import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { themePresets } from '@/lib/themePresets';

const css = readFileSync(resolve(__dirname, '../styles/index.css'), 'utf8');

function variablesIn(selector: string): Map<string, string> {
  const at = css.indexOf(`${selector} {`);
  expect(at, selector).toBeGreaterThanOrEqual(0);
  const body = css.slice(css.indexOf('{', at) + 1, css.indexOf('}', at));
  return new Map(Array.from(body.matchAll(/(--color-[\w-]+):\s*([^;]+);/g), ([, name, value]) => [name, value.trim()]));
}

const defaultVariables = variablesIn(':root');

describe('theme presets in index.css', () => {
  it('keeps the default preset on :root', () => {
    expect(themePresets[0].name).toBe('emerald');
    expect(defaultVariables.get('--color-primary-500')).toBe('16 185 129');
  });

  it.each(themePresets.slice(1).map((preset) => preset.name))('defines every colour variable for "%s"', (name) => {
    const variables = variablesIn(`html[data-theme='${name}']`);
    expect([...variables.keys()].sort()).toEqual([...defaultVariables.keys()].sort());
  });

  it('gives each preset a distinct palette', () => {
    const palettes = themePresets.slice(1).map((preset) => JSON.stringify([...variablesIn(`html[data-theme='${preset.name}']`)]));
    expect(new Set(palettes).size).toBe(palettes.length);
  });
});
