import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HelmetProvider } from 'react-helmet-async';
import { MemoryRouter } from 'react-router-dom';
import i18n from '@/i18n/index';
import Projects from './Projects';

const getAll = vi.hoisted(() => vi.fn());
vi.mock('@/services/api', () => ({
  postsApi: { getAll },
}));
vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: 'en' }),
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));

function renderProjects(url = '/projects') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const view = render(
    <HelmetProvider>
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={[url]}>
          <Projects />
        </MemoryRouter>
      </QueryClientProvider>
    </HelmetProvider>
  );
  return { ...view };
}

const project = {
  id: 'p1',
  slug: 'my-project',
  title: 'My Project',
  excerpt: 'Excerpt',
  hashtags: ['java', 'spring'],
  githubUrl: 'https://github.com/x/y',
  liveUrl: 'https://example.com',
};

describe('Projects', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    getAll.mockReset();
  });

  it('shows the load error with a retry button instead of the empty text when the API fails', async () => {
    getAll.mockRejectedValueOnce(new Error('network'));
    getAll.mockResolvedValueOnce({ success: true, data: [project] });
    renderProjects();

    const retry = await screen.findByRole('button', { name: i18n.t('common.retry') });
    expect(screen.getByText(i18n.t('common.loadError'))).toBeTruthy();
    expect(screen.queryByText(i18n.t('projects.empty'))).toBeNull();

    fireEvent.click(retry);
    expect(await screen.findByText('My Project')).toBeTruthy();
    expect(screen.queryByText(i18n.t('common.loadError'))).toBeNull();
  });

  it('shows the translated empty text when there are no projects', async () => {
    getAll.mockResolvedValueOnce({ success: true, data: [] });
    renderProjects();

    expect(await screen.findByText(i18n.t('projects.empty'))).toBeTruthy();
    expect(i18n.t('projects.empty')).not.toBe('projects.empty');
  });

  it('names the hashtag in the empty text and labels the active filter through i18n', async () => {
    getAll.mockResolvedValueOnce({ success: true, data: [] });
    renderProjects('/projects?hashtag=rust');

    expect(await screen.findByText(i18n.t('projects.emptyForHashtag', { hashtag: 'rust' }))).toBeTruthy();
    expect(screen.getByText(i18n.t('projects.filteredBy'))).toBeTruthy();
    expect(screen.getByRole('button', { name: i18n.t('projects.clearFilter') })).toBeTruthy();
  });

  it('makes the card title a real link and drops the click handler on the article', async () => {
    getAll.mockResolvedValueOnce({ success: true, data: [project] });
    renderProjects();

    const link = await screen.findByRole('link', { name: 'My Project' });
    expect(link.getAttribute('href')).toBe('/projects/my-project');
    expect(link.className).toContain('after:absolute');
    expect(link.className).toContain('after:inset-0');

    const article = link.closest('article')!;
    expect(article.className).toContain('relative');
    expect(article.className).toContain('has-[a:focus-visible]:ring-2');
    expect(article.className).not.toContain('cursor-pointer');
  });

  it('lifts the interactive children above the stretched link', async () => {
    getAll.mockResolvedValueOnce({ success: true, data: [project] });
    renderProjects();

    const hashtag = await screen.findByRole('link', { name: '#java' });
    expect(hashtag.parentElement!.className).toContain('relative z-10');
    const github = screen.getByRole('link', { name: i18n.t('projects.github') });
    expect(github.parentElement!.className).toContain('relative z-10');
  });
});
