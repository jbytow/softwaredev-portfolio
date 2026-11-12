// @vitest-environment node
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';
import type { UserConfig } from 'vite';

// Loaded by path: vite.config.ts belongs to the tsconfig.node.json project, outside the app's include
const frontendRoot = resolve(__dirname, '../..');
const { default: viteConfig } = (await import(/* @vite-ignore */ resolve(frontendRoot, 'vite.config.ts'))) as {
  default: UserConfig;
};

describe('build configuration', () => {
  it('splits vendor code into stable chunks', () => {
    const output = viteConfig.build?.rollupOptions?.output;
    const manualChunks = !Array.isArray(output) ? output?.manualChunks : undefined;
    expect(manualChunks).toBeTypeOf('object');
    expect(Object.keys(manualChunks as object).sort()).toEqual(['data', 'i18n', 'motion', 'react']);
  });

  it('warns about chunks above 350 kB', () => {
    expect(viteConfig.build?.chunkSizeWarningLimit).toBe(350);
  });

  it('type-checks the Vite config in strict mode', () => {
    const nodeTsconfig = JSON.parse(readFileSync(resolve(frontendRoot, 'tsconfig.node.json'), 'utf8'));
    expect(nodeTsconfig.compilerOptions.strict).toBe(true);
  });
});
