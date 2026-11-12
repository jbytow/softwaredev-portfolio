import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';
import i18n from '@/i18n/index';
import AdminLogin from './AdminLogin';

const login = vi.hoisted(() => vi.fn());
vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ login, isAuthenticated: false, isLoading: false, user: null }),
}));
const toastError = vi.hoisted(() => vi.fn());
vi.mock('react-hot-toast', () => ({ default: { error: toastError } }));

function renderAt(url: string) {
  render(
    <HelmetProvider>
      <MemoryRouter initialEntries={[url]}>
        <AdminLogin />
      </MemoryRouter>
    </HelmetProvider>
  );
}

describe('AdminLogin', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  beforeEach(() => {
    login.mockClear();
    toastError.mockClear();
  });
  afterEach(cleanup);

  it.each([
    ['oauth_failed', 'admin.auth.oauthFailed'],
    ['not_authorized', 'admin.auth.notAuthorized'],
  ])('explains the ?error=%s redirect from the backend', (code, key) => {
    renderAt(`/admin/login?error=${code}`);
    expect(toastError).toHaveBeenCalledTimes(1);
    expect(toastError).toHaveBeenCalledWith(i18n.t(key));
  });

  it('shows no error toast on a plain visit or an unknown code', () => {
    renderAt('/admin/login');
    renderAt('/admin/login?error=something_else');
    expect(toastError).not.toHaveBeenCalled();
  });

  it('starts the Google login without a provider argument', () => {
    renderAt('/admin/login');
    fireEvent.click(screen.getByRole('button', { name: /google/i }));
    expect(login).toHaveBeenCalledTimes(1);
    expect(login).toHaveBeenCalledWith();
  });
});
