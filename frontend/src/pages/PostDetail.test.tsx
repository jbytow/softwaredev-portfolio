import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HelmetProvider } from 'react-helmet-async';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import i18n from '@/i18n/index';
import PostDetail from './PostDetail';

const getBySlug = vi.hoisted(() => vi.fn());
vi.mock('@/services/api', () => ({
  postsApi: { getBySlug },
}));
vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: 'en' }),
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));

function renderPost() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <HelmetProvider>
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={['/projects/demo']}>
          <Routes>
            <Route path="/projects/:slug" element={<PostDetail />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    </HelmetProvider>
  );
}

const post = {
  id: 'p1',
  slug: 'demo',
  title: 'Demo project',
  excerpt: 'Excerpt',
  content: '',
  hashtags: [],
  githubUrl: 'https://github.com/x/y',
  liveUrl: 'https://example.com',
};

function httpError(status: number) {
  return Object.assign(new Error(`HTTP ${status}`), { response: { status } });
}

describe('PostDetail', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    getBySlug.mockReset();
  });

  it.each([500, 503])('offers a retry instead of "not found" on a %s response', async (status) => {
    getBySlug.mockRejectedValueOnce(httpError(status));
    getBySlug.mockResolvedValueOnce({ success: true, data: post });
    renderPost();

    const retry = await screen.findByRole('button', { name: i18n.t('common.retry') });
    expect(screen.getByText(i18n.t('common.loadError'))).toBeTruthy();
    expect(screen.queryByText(i18n.t('post.notFound'))).toBeNull();

    fireEvent.click(retry);
    expect(await screen.findByRole('heading', { name: 'Demo project' })).toBeTruthy();
  });

  it('offers a retry on a network error without a response', async () => {
    getBySlug.mockRejectedValueOnce(new Error('Network Error'));
    renderPost();

    expect(await screen.findByRole('button', { name: i18n.t('common.retry') })).toBeTruthy();
    expect(screen.queryByText(i18n.t('post.notFound'))).toBeNull();
  });

  it('shows "not found" with a full-sentence back link only for a 404', async () => {
    getBySlug.mockRejectedValueOnce(httpError(404));
    renderPost();

    expect(await screen.findByText(i18n.t('post.notFound'))).toBeTruthy();
    expect(i18n.t('post.notFound')).not.toBe('post.notFound');
    expect(screen.queryByRole('button', { name: i18n.t('common.retry') })).toBeNull();
    const back = screen.getByRole('link', { name: i18n.t('post.backToProjects') });
    expect(back.getAttribute('href')).toBe('/projects');
  });

  it('labels the back link and the external links through i18n', async () => {
    getBySlug.mockResolvedValueOnce({ success: true, data: post });
    renderPost();

    expect(await screen.findByRole('link', { name: i18n.t('post.backToProjects') })).toBeTruthy();
    expect(screen.getByRole('link', { name: i18n.t('projects.github') })).toBeTruthy();
    expect(screen.getByRole('link', { name: i18n.t('projects.liveDemo') })).toBeTruthy();
  });
});
