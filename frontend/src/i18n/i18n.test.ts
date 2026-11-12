import { describe, expect, it } from 'vitest';
import en from '@/i18n/en.json';
import pl from '@/i18n/pl.json';

type Tree = { [key: string]: string | Tree };

function flatten(tree: Tree, prefix = ''): Record<string, string> {
  return Object.entries(tree).reduce<Record<string, string>>((acc, [key, value]) => {
    if (typeof value === 'string') acc[`${prefix}${key}`] = value;
    else Object.assign(acc, flatten(value, `${prefix}${key}.`));
    return acc;
  }, {});
}

const enFlat = flatten(en as Tree);
const plFlat = flatten(pl as Tree);

const NERD_SUFFIX = '_nerd';

// Keys that exist in both languages but have no consumer yet; the packages that move literals onto
// keys remove entries as they land. The test fails on a stale entry, so the list can only shrink.
const PENDING_CONSUMERS: string[] = [
  'common.loading',
  'common.error',
  'common.edit',
  'common.create',
  'common.filter',
  'common.back',
  'common.learnMore',
  'common.downloadCV',
  'nav.admin',
  'home.stats.projects',
  'home.stats.technologies',
  'home.stats.commits',
  'home.stats.experience',
  'about.rpg.str',
  'about.rpg.int',
  'about.rpg.wis',
  'about.rpg.dex',
  'about.rpg.con',
  'about.rpg.cha',
  'admin.auth.login',
  'admin.auth.github',
];

const sources = import.meta.glob<string>(['/src/**/*.{ts,tsx}', '!/src/**/*.test.{ts,tsx}'], {
  query: '?raw',
  import: 'default',
  eager: true,
});

// t('a.b'), i18n.t('a.b'), t(`a.${x}`) and labelKey: 'a.b' (nav tables resolved later through t(item.labelKey))
const KEY_PATTERN = /(?:\bt\(\s*|\blabelKey:\s*)(['"`])([^'"`]+?)\1/g;

function usedKeys(): { file: string; key: string }[] {
  return Object.entries(sources).flatMap(([file, code]) =>
    Array.from(code.matchAll(KEY_PATTERN), (match) => ({ file, key: match[2] }))
  );
}

function resolves(key: string): boolean {
  const dynamicAt = key.indexOf('${');
  if (dynamicAt === -1) return key in enFlat;
  const prefix = key.slice(0, dynamicAt);
  return Object.keys(enFlat).some((existing) => existing.startsWith(prefix));
}

// A quoted literal also covers keys picked by a ternary or kept in a nav table, which KEY_PATTERN does not see
function isQuotedIn(code: string, key: string): boolean {
  return ['"', "'", '`'].some((quote) => code.includes(`${quote}${key}${quote}`));
}

function unreferencedKeys(): string[] {
  const code = Object.values(sources).join('\n');
  const dynamicPrefixes = usedKeys()
    .map(({ key }) => key)
    .filter((key) => key.includes('${'))
    .map((key) => key.slice(0, key.indexOf('${')));
  return Object.keys(enFlat).filter((key) => {
    // useNerdTranslation resolves X_nerd on its own whenever X is requested
    const base = key.endsWith(NERD_SUFFIX) ? key.slice(0, -NERD_SUFFIX.length) : key;
    return !dynamicPrefixes.some((prefix) => base.startsWith(prefix)) && !isQuotedIn(code, base);
  });
}

describe('i18n resources', () => {
  it('en.json and pl.json have identical keys', () => {
    expect(Object.keys(plFlat).sort()).toEqual(Object.keys(enFlat).sort());
  });

  it('has no empty translation', () => {
    const empty = [...Object.entries(enFlat), ...Object.entries(plFlat)]
      .filter(([, value]) => value.trim() === '')
      .map(([key]) => key);
    expect(empty).toEqual([]);
  });

  it('defines every key used in src', () => {
    const used = usedKeys();
    expect(used.length).toBeGreaterThan(50);
    const missing = used.filter(({ key }) => !resolves(key)).map(({ file, key }) => `${file}: ${key}`);
    expect(missing).toEqual([]);
  });

  it('gives every Nerd Mode variant a base key to fall back from', () => {
    const orphaned = Object.keys(enFlat).filter(
      (key) => key.endsWith(NERD_SUFFIX) && !(key.slice(0, -NERD_SUFFIX.length) in enFlat)
    );
    expect(orphaned).toEqual([]);
  });

  it('has no key without a consumer outside PENDING_CONSUMERS', () => {
    const unused = unreferencedKeys().filter((key) => !PENDING_CONSUMERS.includes(key));
    expect(unused).toEqual([]);
  });

  it('keeps PENDING_CONSUMERS free of keys that already have a consumer or no longer exist', () => {
    const unused = new Set(unreferencedKeys());
    const stale = PENDING_CONSUMERS.filter((key) => !unused.has(key));
    expect(stale).toEqual([]);
  });
});
