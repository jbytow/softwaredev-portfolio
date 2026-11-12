import { describe, expect, it } from 'vitest';
import { normalizeLinkHref } from './richTextExtensions';

describe('normalizeLinkHref', () => {
  it.each([
    ['example.com', 'https://example.com'],
    ['www.example.com/path?q=1', 'https://www.example.com/path?q=1'],
    ['brand.pl/kampania', 'https://brand.pl/kampania'],
    ['  example.com  ', 'https://example.com'],
  ])('adds https:// to %j', (href, expected) => {
    expect(normalizeLinkHref(href)).toBe(expected);
  });

  it.each([
    'https://x',
    'http://example.com',
    'HTTPS://EXAMPLE.COM',
    'mailto:a@b',
    'tel:+48123456789',
    '/about',
    '//cdn.example.com/file.pdf',
    '#section',
  ])('keeps %j unchanged', (href) => {
    expect(normalizeLinkHref(href)).toBe(href);
  });
});
