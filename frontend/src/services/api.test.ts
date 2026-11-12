import { describe, expect, it } from 'vitest';
import * as api from './api';

// The backend no longer serves these public routes, so a wrapper would only produce 404s.
const REMOVED_WRAPPERS: [string, string | null][] = [
  ['softSkillsApi', null],
  ['categoriesApi', null],
  ['postsApi', 'getPaged'],
  ['postsApi', 'getHashtags'],
  ['skillCategoriesApi', 'getAll'],
  ['skillCategoriesApi', 'getById'],
  ['experiencesApi', 'getById'],
  ['interestsApi', 'getById'],
  ['achievementsApi', 'getById'],
  ['rpgStatsApi', 'getById'],
];

const exported = api as unknown as Record<string, Record<string, unknown> | undefined>;

describe('public api wrappers', () => {
  it.each(REMOVED_WRAPPERS)('has no %s.%s', (group, method) => {
    if (method === null) expect(exported[group]).toBeUndefined();
    else expect(exported[group]?.[method]).toBeUndefined();
  });

  it('keeps the wrappers the public pages call', () => {
    expect(typeof api.postsApi.getAll).toBe('function');
    expect(typeof api.postsApi.getBySlug).toBe('function');
    expect(typeof api.skillCategoriesApi.getAllWithSkills).toBe('function');
    expect(typeof api.experiencesApi.getAll).toBe('function');
    expect(typeof api.interestsApi.getAll).toBe('function');
    expect(typeof api.achievementsApi.getAll).toBe('function');
    expect(typeof api.rpgStatsApi.getAll).toBe('function');
  });
});
