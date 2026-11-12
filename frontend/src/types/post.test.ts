import { describe, expectTypeOf, it } from 'vitest';
import type { Post } from './index';

// Public responses carry only the requested language, so the other language's raw fields arrive as null.
describe('Post raw language fields', () => {
  it('are nullable', () => {
    expectTypeOf<Post['titleEn']>().toEqualTypeOf<string | null>();
    expectTypeOf<Post['titlePl']>().toEqualTypeOf<string | null>();
    expectTypeOf<Post['excerptEn']>().toEqualTypeOf<string | null>();
    expectTypeOf<Post['excerptPl']>().toEqualTypeOf<string | null>();
    expectTypeOf<Post['contentEn']>().toEqualTypeOf<Record<string, unknown> | null>();
    expectTypeOf<Post['contentPl']>().toEqualTypeOf<Record<string, unknown> | null>();
  });
});
