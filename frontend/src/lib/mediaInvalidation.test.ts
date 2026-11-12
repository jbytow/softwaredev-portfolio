import { describe, expect, it, vi } from 'vitest';
import { QueryClient } from '@tanstack/react-query';
import { invalidateMediaConsumers } from './mediaInvalidation';
import { queryKeys } from './queryKeys';

describe('invalidateMediaConsumers', () => {
  it('invalidates every public and admin query that embeds media', () => {
    const client = new QueryClient();
    const spy = vi.spyOn(client, 'invalidateQueries');

    invalidateMediaConsumers(client);

    const keys = spy.mock.calls.map(([filters]) => JSON.stringify(filters?.queryKey));
    for (const expected of [
      queryKeys.prefix.posts(),
      queryKeys.prefix.post(),
      queryKeys.prefix.interests(),
      queryKeys.prefix.settings(),
      queryKeys.admin.postsAll(),
      queryKeys.admin.postAll(),
      queryKeys.admin.interests(),
      queryKeys.admin.settings(),
    ]) {
      expect(keys).toContain(JSON.stringify(expected));
    }
  });
});
