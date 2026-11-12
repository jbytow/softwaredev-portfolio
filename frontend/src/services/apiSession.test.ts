import axios, { type AxiosAdapter } from 'axios';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

// axios.create() copies axios.defaults, so the stub adapter must be installed before api.ts is imported.
let respondWith401 = false;
const stubAdapter: AxiosAdapter = async (config) => {
  if (respondWith401) {
    const response = { data: {}, status: 401, statusText: 'Unauthorized', headers: {}, config };
    throw new axios.AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, null, response);
  }
  return { data: { success: true }, status: 200, statusText: 'OK', headers: {}, config };
};

let api: typeof import('./api');
let events: typeof import('./api');

beforeAll(async () => {
  axios.defaults.adapter = stubAdapter;
  api = await import('./api');
  events = api;
});

describe('expired session (401)', () => {
  const onExpired = vi.fn();

  beforeEach(() => {
    onExpired.mockClear();
    window.addEventListener(events.AUTH_EXPIRED_EVENT, onExpired);
    respondWith401 = true;
  });
  afterEach(() => window.removeEventListener(events.AUTH_EXPIRED_EVENT, onExpired));

  it('announces the expiry instead of navigating away from an admin form', async () => {
    window.history.pushState({}, '', '/admin/posts/new');
    await expect(api.adminPostsApi.getAll()).rejects.toMatchObject({ response: { status: 401 } });
    expect(onExpired).toHaveBeenCalledTimes(1);
    expect(window.location.pathname).toBe('/admin/posts/new');
  });

  it('does not announce a failed initial /admin/auth/me check', async () => {
    window.history.pushState({}, '', '/admin');
    await expect(api.authApi.getCurrentUser()).rejects.toBeTruthy();
    expect(onExpired).not.toHaveBeenCalled();
  });

  it.each(['/', '/projects', '/admin/login'])('stays silent on %s', async (path) => {
    window.history.pushState({}, '', path);
    await expect(api.adminPostsApi.getAll()).rejects.toBeTruthy();
    expect(onExpired).not.toHaveBeenCalled();
  });

  it('keeps other responses out of the expiry flow', async () => {
    respondWith401 = false;
    window.history.pushState({}, '', '/admin/posts');
    await api.adminPostsApi.getAll();
    expect(onExpired).not.toHaveBeenCalled();
  });
});
