import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import i18n from '@/i18n/index';
import Header from './Header';

const toggleNerdMode = vi.fn();
const cycleTheme = vi.fn();
let nerdMode = false;

vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: 'en', toggleLanguage: vi.fn() }),
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode, toggleNerdMode }),
}));
vi.mock('@/contexts/ThemeContext', () => ({
  useTheme: () => ({ theme: 'emerald', cycleTheme }),
}));
vi.mock('@/services/api', () => ({
  settingsApi: { get: () => Promise.resolve({ data: { siteName: 'Portfolio' } }) },
}));

let navigate: ReturnType<typeof useNavigate>;
function Probe() {
  navigate = useNavigate();
  return null;
}

function renderHeader(initialEntry = '/') {
  render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/start', initialEntry]} initialIndex={1}>
        <Header />
        <Routes>
          <Route path="*" element={<Probe />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

function toggle() {
  return screen.getByRole('button', { name: i18n.t('nav.toggleMenu') });
}

vi.stubGlobal('scrollTo', vi.fn());

describe('Header mobile menu', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    nerdMode = false;
  });

  it('opens and closes with the toggle and keeps aria-expanded in sync', () => {
    renderHeader();
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
    fireEvent.click(toggle());
    expect(toggle().getAttribute('aria-expanded')).toBe('true');
    fireEvent.click(toggle());
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes when the route changes without a click in the menu (back button, programmatic)', () => {
    renderHeader('/about');
    fireEvent.click(toggle());
    expect(toggle().getAttribute('aria-expanded')).toBe('true');

    act(() => navigate('/projects'));
    expect(toggle().getAttribute('aria-expanded')).toBe('false');

    fireEvent.click(toggle());
    act(() => navigate(-1));
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes when the logo is clicked even if the path does not change', () => {
    renderHeader('/');
    fireEvent.click(toggle());
    fireEvent.click(screen.getByRole('link', { name: 'Portfolio' }));
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes on Escape and returns focus to the toggle', () => {
    renderHeader();
    fireEvent.click(toggle());
    expect(document.activeElement).not.toBe(toggle());

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
    expect(document.activeElement).toBe(toggle());
  });

  it('does not steal focus on Escape while the menu is closed', () => {
    renderHeader();
    const other = screen.getByRole('link', { name: 'Portfolio' });
    other.focus();
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(document.activeElement).toBe(other);
  });
});

describe('Header control labels', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    nerdMode = false;
  });

  it('names every icon button from the translation files', () => {
    renderHeader();
    for (const key of ['nav.cycleTheme', 'nav.toggleNerdMode', 'nav.toggleLanguage', 'nav.toggleMenu']) {
      expect(screen.getByRole('button', { name: i18n.t(key) }), key).toBeTruthy();
    }
  });

  it('translates the labels into Polish', async () => {
    await i18n.changeLanguage('pl');
    renderHeader();
    for (const key of ['nav.cycleTheme', 'nav.toggleNerdMode', 'nav.toggleLanguage', 'nav.toggleMenu']) {
      expect(i18n.t(key), key).not.toBe(key);
      expect(screen.getByRole('button', { name: i18n.t(key) }), key).toBeTruthy();
    }
    await i18n.changeLanguage('en');
  });

  it('reports Nerd mode as a pressed toggle button', () => {
    renderHeader();
    expect(screen.getByRole('button', { name: i18n.t('nav.toggleNerdMode') }).getAttribute('aria-pressed')).toBe('false');
    cleanup();

    nerdMode = true;
    renderHeader();
    expect(screen.getByRole('button', { name: i18n.t('nav.toggleNerdMode') }).getAttribute('aria-pressed')).toBe('true');
  });
});
