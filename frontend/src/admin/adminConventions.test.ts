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
    // AdminLayout asks before leaving a dirty form (not a delete), with the same prompt as useUnsavedChanges
    const offenders = linesMatching(/(?<![\w.])(window\.)?confirm\(/).filter((entry) => !entry.includes('AdminLayout.tsx'));
    expect(offenders).toEqual([]);
  });

  it('invalidates through queryKeys, not hand-written key arrays', () => {
    expect(linesMatching(/invalidateQueries\(\{ queryKey: \[/)).toEqual([]);
  });

  it('refreshes the public pages (queryKeys.prefix) wherever an admin mutation invalidates admin data', () => {
    const offenders = Object.entries(sources)
      .filter(([file, code]) => /invalidateQueries\(\{ queryKey: queryKeys\.admin\./.test(code) && !file.endsWith('AdminMedia.tsx'))
      .filter(([, code]) => !/queryKeys\.prefix\./.test(code))
      .map(([file]) => file);
    expect(offenders).toEqual([]);
  });
});
