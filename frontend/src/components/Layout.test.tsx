import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom';
import i18n from '@/i18n/index';
import Layout from './Layout';

// Header and footer need their own providers and queries; stubs keep their landmarks visible
vi.mock('./Header', () => ({ default: () => <header>site header</header> }));
vi.mock('./Footer', () => ({ default: () => <footer>site footer</footer> }));

let navigate: ReturnType<typeof useNavigate>;
function Probe() {
  navigate = useNavigate();
  return null;
}

function Broken(): never {
  throw new Error('page crashed');
}

function renderLayout(initialEntry = '/list') {
  return render(
    <MemoryRouter initialEntries={['/start', initialEntry]} initialIndex={1}>
      <Probe />
      <Routes>
        <Route element={<Layout />}>
          <Route path="/broken" element={<Broken />} />
          <Route path="*" element={<p>page</p>} />
        </Route>
      </Routes>
    </MemoryRouter>
  );
}

describe('Layout', () => {
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

  it('offers a skip link that targets the focusable main landmark', () => {
    renderLayout();
    const skip = screen.getByRole('link', { name: i18n.t('common.skipToContent') });
    expect(skip.getAttribute('href')).toBe('#main');

    const main = screen.getByRole('main');
    expect(main.id).toBe('main');
    expect(main.getAttribute('tabindex')).toBe('-1');
  });

  it('puts the skip link before the header in tab order', () => {
    renderLayout();
    const skip = screen.getByRole('link', { name: i18n.t('common.skipToContent') });
    const header = screen.getByText('site header');
    expect(skip.compareDocumentPosition(header) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });

  it('keeps header and footer when a page crashes and shows the error screen inside main', () => {
    renderLayout('/broken');
    expect(screen.getByText('site header')).toBeTruthy();
    expect(screen.getByText('site footer')).toBeTruthy();
    expect(screen.getByRole('main').textContent).toContain(i18n.t('errors.boundary.title'));
  });

  it('clears the error screen when navigating to another page', () => {
    renderLayout('/broken');
    expect(screen.queryByText('page')).toBeNull();

    act(() => navigate('/fine'));
    expect(screen.getByText('page')).toBeTruthy();
    expect(screen.getByRole('main').textContent).not.toContain(i18n.t('errors.boundary.title'));
  });
});
