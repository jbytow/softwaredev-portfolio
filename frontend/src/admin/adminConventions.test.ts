import { describe, expect, it } from 'vitest';

const sources = import.meta.glob<string>(
  ['/src/admin/**/*.tsx', '!/src/admin/**/*.test.tsx'],
  { query: '?raw', import: 'default', eager: true }
);

function linesMatching(pattern: RegExp): string[] {
  return Object.entries(sources).flatMap(([file, code]) =>
    code
      .split('\n')
      .map((line, index) => ({ line, at: `${file}:${index + 1}` }))
      .filter(({ line }) => pattern.test(line))
      .map(({ at, line }) => `${at} ${line.trim()}`)
  );
}

describe('admin panel conventions', () => {
  it('shows server error messages in every error toast (except the login redirect messages)', () => {
    const offenders = linesMatching(/toast\.error\(/).filter(
      (entry) => !entry.includes('getApiErrorMessage(') && !entry.includes('AdminLogin.tsx')
    );
    expect(offenders).toEqual([]);
  });

  it('confirms deletes through ConfirmDeleteModal, not native confirm()', () => {
    expect(linesMatching(/(?<![\w.])(window\.)?confirm\(/)).toEqual([]);
  });
});
