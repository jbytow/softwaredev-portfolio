import { describe, expect, it } from 'vitest';
import { queryKeys } from './queryKeys';

const startsWith = (key: readonly unknown[], prefix: readonly unknown[]) =>
  prefix.every((part, index) => key[index] === part);

describe('queryKeys.prefix', () => {
  it('prefixes every public per-language key, so one invalidation covers all languages', () => {
    const pairs: Array<[readonly unknown[], readonly unknown[]]> = [
      [queryKeys.settings('en'), queryKeys.prefix.settings()],
      [queryKeys.posts('pl'), queryKeys.prefix.posts()],
      [queryKeys.posts('pl', 'featured'), queryKeys.prefix.posts()],
      [queryKeys.postsByHashtag('pl', 'java'), queryKeys.prefix.posts()],
      [queryKeys.post('en', 'slug'), queryKeys.prefix.post()],
      [queryKeys.skillCategoriesWithSkills('en'), queryKeys.prefix.skills()],
      [queryKeys.experiences('en'), queryKeys.prefix.experiences()],
      [queryKeys.interests('en'), queryKeys.prefix.interests()],
      [queryKeys.achievements('en'), queryKeys.prefix.achievements()],
      [queryKeys.rpgStats('en'), queryKeys.prefix.rpgStats()],
    ];
    for (const [key, prefix] of pairs) {
      expect(startsWith(key, prefix)).toBe(true);
    }
  });
});

describe('queryKeys.admin', () => {
  it('keys media per page and exposes a prefix for all pages', () => {
    expect(startsWith(queryKeys.admin.media(2, 100), queryKeys.admin.mediaAll())).toBe(true);
    expect(queryKeys.admin.media(0, 100)).not.toEqual(queryKeys.admin.media(1, 100));
  });

  it('exposes a posts prefix that matches category-filtered lists', () => {
    expect(startsWith(queryKeys.admin.posts('PROJECT' as never), queryKeys.admin.postsAll())).toBe(true);
    expect(startsWith(queryKeys.admin.posts(), queryKeys.admin.postsAll())).toBe(true);
  });
});
