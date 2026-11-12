import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const read = (relative: string) => readFileSync(resolve(__dirname, '../..', relative), 'utf8');
const css = read('src/styles/index.css');

// The gate is the last block of the stylesheet, so everything from its opening is the block
const reducedMotionBlock = css.slice(css.indexOf('@media (prefers-reduced-motion: reduce)'));

describe('prefers-reduced-motion', () => {
  it('has a CSS gate that reaches every element and pseudo-element', () => {
    expect(css).toContain('@media (prefers-reduced-motion: reduce)');
    expect(reducedMotionBlock).toMatch(/\*,\s*\*::before,\s*\*::after/);
  });

  it('shortens animations and transitions instead of removing them, so end states still apply', () => {
    // animation: none would leave the fatigue-check reveal stuck at opacity 0
    expect(reducedMotionBlock).toMatch(/animation-duration:\s*0\.01ms\s*!important/);
    expect(reducedMotionBlock).toMatch(/animation-iteration-count:\s*1\s*!important/);
    expect(reducedMotionBlock).toMatch(/transition-duration:\s*0\.01ms\s*!important/);
    expect(reducedMotionBlock).not.toMatch(/animation:\s*none/);
  });

  it('turns off smooth scrolling', () => {
    expect(reducedMotionBlock).toMatch(/scroll-behavior:\s*auto\s*!important/);
  });

  it('configures framer-motion to follow the user preference for the whole app', () => {
    expect(read('src/main.tsx')).toMatch(/<MotionConfig reducedMotion="user">/);
  });
});
