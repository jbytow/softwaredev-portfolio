import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { act, cleanup, render } from '@testing-library/react';
import { themePresets } from '@/lib/themePresets';
import { ThemeProvider, useTheme } from './ThemeContext';

let current: ReturnType<typeof useTheme>;
function Probe() {
  current = useTheme();
  return null;
}

function renderProvider() {
  render(
    <ThemeProvider>
      <Probe />
    </ThemeProvider>
  );
}

const root = document.documentElement;

describe('ThemeProvider', () => {
  beforeEach(() => {
    localStorage.clear();
    root.removeAttribute('data-theme');
    root.removeAttribute('style');
  });
  afterEach(cleanup);

  it('marks the document with the saved theme', () => {
    localStorage.setItem('theme', 'coffee');
    renderProvider();
    expect(root.getAttribute('data-theme')).toBe('coffee');
  });

  it('starts on the first preset when nothing is saved', () => {
    renderProvider();
    expect(root.getAttribute('data-theme')).toBe(themePresets[0].name);
    expect(localStorage.getItem('theme')).toBe(themePresets[0].name);
  });

  it('falls back to the first preset for an unknown saved name', () => {
    localStorage.setItem('theme', 'nope');
    renderProvider();
    expect(root.getAttribute('data-theme')).toBe(themePresets[0].name);
  });

  it('switches the attribute and storage together, cycling through every preset', () => {
    renderProvider();
    for (const preset of themePresets.slice(1)) {
      act(() => current.cycleTheme());
      expect(root.getAttribute('data-theme')).toBe(preset.name);
      expect(localStorage.getItem('theme')).toBe(preset.name);
    }
    act(() => current.cycleTheme());
    expect(root.getAttribute('data-theme')).toBe(themePresets[0].name);
  });

  it('leaves colour variables to the stylesheet instead of inline styles', () => {
    renderProvider();
    act(() => current.setTheme('neon'));
    expect(root.style.getPropertyValue('--color-primary-500')).toBe('');
  });
});
