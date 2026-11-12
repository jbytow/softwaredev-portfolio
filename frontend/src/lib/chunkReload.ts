export const CHUNK_RELOAD_KEY = 'chunk-reload-at';
const RELOAD_GUARD_MS = 10_000;

// A tab opened before a deploy may request chunks that were pruned since; reloading fetches the
// current index.html. The timestamp guard stops a reload loop if the chunk is missing for real:
// the repeated error then reaches the error boundary, whose button reloads on demand.
export function handlePreloadError(event: Event) {
  const lastReload = Number(sessionStorage.getItem(CHUNK_RELOAD_KEY) ?? 0);
  if (Date.now() - lastReload < RELOAD_GUARD_MS) return;
  sessionStorage.setItem(CHUNK_RELOAD_KEY, String(Date.now()));
  event.preventDefault();
  window.location.reload();
}
