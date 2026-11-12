import type { QueryClient } from '@tanstack/react-query';
import { queryKeys } from './queryKeys';

// Media is embedded in posts, interests and site settings, so deleting a file must refresh
// every query that may still show it (public and admin), not only the media library itself.
export function invalidateMediaConsumers(queryClient: QueryClient): void {
  queryClient.invalidateQueries({ queryKey: queryKeys.prefix.posts() });
  queryClient.invalidateQueries({ queryKey: queryKeys.prefix.post() });
  queryClient.invalidateQueries({ queryKey: queryKeys.prefix.interests() });
  queryClient.invalidateQueries({ queryKey: queryKeys.prefix.settings() });
  queryClient.invalidateQueries({ queryKey: queryKeys.admin.postsAll() });
  queryClient.invalidateQueries({ queryKey: queryKeys.admin.postAll() });
  queryClient.invalidateQueries({ queryKey: queryKeys.admin.interests() });
  queryClient.invalidateQueries({ queryKey: queryKeys.admin.settings() });
}
