import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { CHUNK_RELOAD_KEY, handlePreloadError } from './chunkReload';

function preloadEvent() {
  return new Event('vite:preloadError', { cancelable: true });
}

describe('handlePreloadError', () => {
  const reload = vi.fn();

  beforeEach(() => {
    sessionStorage.clear();
    reload.mockReset();
    vi.stubGlobal('location', { ...window.location, reload });
  });
  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('reloads once and swallows the error so the old page does not flash an error screen', () => {
    const event = preloadEvent();
    handlePreloadError(event);

    expect(reload).toHaveBeenCalledTimes(1);
    expect(event.defaultPrevented).toBe(true);
    expect(sessionStorage.getItem(CHUNK_RELOAD_KEY)).not.toBeNull();
  });

  it('lets the error through when it repeats right after a reload, so the error screen replaces a loop', () => {
    handlePreloadError(preloadEvent());
    reload.mockClear();

    const event = preloadEvent();
    handlePreloadError(event);

    expect(reload).not.toHaveBeenCalled();
    expect(event.defaultPrevented).toBe(false);
  });

  it('reloads again for a later deploy once the guard window has passed', () => {
    vi.useFakeTimers();
    handlePreloadError(preloadEvent());
    reload.mockClear();

    vi.advanceTimersByTime(60_000);
    handlePreloadError(preloadEvent());

    expect(reload).toHaveBeenCalledTimes(1);
  });
});
