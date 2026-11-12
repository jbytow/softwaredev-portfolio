import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter, Outlet, useNavigate } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';
import i18n from '@/i18n/index';
import App from './App';

vi.mock('./components/Header', () => ({ default: () => <header>site header</header> }));
vi.mock('./components/Footer', () => ({ default: () => <footer>site footer</footer> }));
vi.mock('./components/ProtectedRoute', () => ({ default: ({ children }: { children: React.ReactNode }) => children }));
vi.mock('./admin/AdminLayout', () => ({
  default: () => (
    <div>
      <nav>admin menu</nav>
      <Outlet />
    </div>
  ),
}));
vi.mock('./admin/AdminAchievementForm', () => ({
  default: () => {
    throw new Error('form crashed');
  },
}));
vi.mock('./admin/AdminRpgStats', () => ({ default: () => <p>rpg stats list</p> }));

let navigate: ReturnType<typeof useNavigate>;
function Probe() {
  navigate = useNavigate();
  return null;
}

function renderApp(path: string) {
  return render(
    <HelmetProvider>
      <MemoryRouter initialEntries={[path]}>
        <Probe />
        <App />
      </MemoryRouter>
    </HelmetProvider>
  );
}

describe('App routes', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  beforeEach(() => {
    window.scrollTo = vi.fn();
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('renders the 404 page inside the public layout for an unknown path', async () => {
    renderApp('/no/such/page');
    expect(await screen.findByRole('heading', { name: i18n.t('notFound.title') })).toBeTruthy();
    expect(screen.getByText('site header')).toBeTruthy();
  });

  it('keeps the admin layout when a lazy admin page crashes and recovers on navigation', async () => {
    renderApp('/admin/achievements/new');
    expect(await screen.findByText(i18n.t('errors.boundary.title'))).toBeTruthy();
    expect(screen.getByText('admin menu')).toBeTruthy();

    act(() => navigate('/admin/rpg-stats'));
    expect(await screen.findByText('rpg stats list')).toBeTruthy();
    expect(screen.queryByText(i18n.t('errors.boundary.title'))).toBeNull();
  });
});
