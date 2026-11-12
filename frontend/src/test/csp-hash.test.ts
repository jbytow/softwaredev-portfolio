import { describe, expect, it } from 'vitest';
import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

// The edge CSP allows the inline theme script in index.html only through its sha256 hash
// (nginx/nginx.conf, script-src). Editing the script without updating the hash would make
// every page view a CSP violation, so the two files are checked against each other here.
function inlineScriptHash(html: string): string {
  const match = html.match(/<script>([\s\S]*?)<\/script>/);
  if (!match) throw new Error('index.html has no inline script');
  return `sha256-${createHash('sha256').update(match[1], 'utf8').digest('base64')}`;
}

describe('CSP hash of the inline theme script', () => {
  const html = readFileSync(resolve(__dirname, '../../index.html'), 'utf8');
  const nginx = readFileSync(resolve(__dirname, '../../../nginx/nginx.conf'), 'utf8');

  it('matches the sha256 source listed in script-src of the edge CSP', () => {
    const scriptSrc = nginx.match(/script-src([^;]*);/);
    expect(scriptSrc, 'nginx.conf has no script-src directive').toBeTruthy();
    expect(scriptSrc![1]).toContain(`'${inlineScriptHash(html)}'`);
  });

  it('changes when the script changes (hash is not a constant)', () => {
    const tampered = html.replace('<script>', "<script>/* x */");
    expect(inlineScriptHash(tampered)).not.toBe(inlineScriptHash(html));
  });
});
