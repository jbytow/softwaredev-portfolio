import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';
import i18n from '@/i18n/index';
import type { Post } from '@/types';
import AdminPostPreview from './AdminPostPreview';
import AdminPosts from './AdminPosts';
import AdminPostForm from './AdminPostForm';

const api = vi.hoisted(() => ({ getById: vi.fn(), getAll: vi.fn() }));
vi.mock('@/services/api', () => ({ adminPostsApi: api, adminMediaApi: {} }));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));

const draft = {
  id: 'p1',
  category: 'PERSONAL_PROJECT',
  title: 'ignored server-resolved title',
  titleEn: 'English draft title',
  titlePl: 'Polski roboczy tytul',
  slug: 'draft-post',
  excerpt: null,
  excerptEn: 'English excerpt',
  excerptPl: 'Polski wstep',
  content: null,
  contentEn: null,
  contentPl: null,
  featuredImage: null,
  published: false,
  hashtags: ['react'],
  media: [],
  hasCaseStudy: false,
  caseStudyChallenge: null,
  caseStudyChallengeEn: 'English challenge',
  caseStudyChallengePl: null,
  caseStudySolution: null,
  caseStudySolutionEn: null,
  caseStudySolutionPl: null,
  caseStudyResults: null,
  caseStudyResultsEn: null,
  caseStudyResultsPl: null,
  caseStudyTestimonial: null,
  caseStudyTestimonialEn: null,
  caseStudyTestimonialPl: null,
  caseStudyTestimonialAuthor: null,
} as unknown as Post;

function renderPreview() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <HelmetProvider>
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={['/admin/posts/p1/preview']}>
          <Routes>
            <Route path="/admin/posts/:id/preview" element={<AdminPostPreview />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    </HelmetProvider>
  );
}

const englishTitle = () => screen.findByRole('heading', { level: 1, name: 'English draft title' });

describe('AdminPostPreview', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  beforeEach(() => {
    api.getById.mockResolvedValue({ success: true, data: draft });
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('loads the draft by id through the admin API and shows the draft banner', async () => {
    renderPreview();

    expect(await englishTitle()).toBeTruthy();
    expect(api.getById).toHaveBeenCalledWith('p1');
    expect(screen.getByText(i18n.t('admin.preview.draftBanner'))).toBeTruthy();
  });

  it('links back to the form of the same post', async () => {
    renderPreview();
    await englishTitle();

    const back = screen.getByRole('link', { name: i18n.t('admin.preview.backToEditor') });
    expect(back.getAttribute('href')).toBe('/admin/posts/p1/edit');
  });

  it('switches the previewed language and shows only that language fields', async () => {
    renderPreview();
    await englishTitle();
    expect(screen.getByText('English excerpt')).toBeTruthy();

    fireEvent.click(screen.getByRole('button', { name: 'PL' }));

    expect(screen.getByRole('heading', { level: 1, name: 'Polski roboczy tytul' })).toBeTruthy();
    expect(screen.getByText('Polski wstep')).toBeTruthy();
    expect(screen.queryByText('English draft title')).toBeNull();
    expect(screen.queryByText('English excerpt')).toBeNull();

    fireEvent.click(screen.getByRole('button', { name: 'EN' }));
    expect(screen.getByRole('heading', { level: 1, name: 'English draft title' })).toBeTruthy();
  });

  it('shows the case study section only in a language that has case study text, as the public page does', async () => {
    renderPreview();
    await englishTitle();
    expect(screen.getByRole('button', { name: i18n.t('caseStudy.title') })).toBeTruthy();

    fireEvent.click(screen.getByRole('button', { name: 'PL' }));

    expect(screen.queryByRole('button', { name: i18n.t('caseStudy.title') })).toBeNull();
    expect(screen.queryByRole('button', { name: i18n.t('caseStudy.title', { lng: 'pl' }) })).toBeNull();
  });

  it('translates the page labels into the previewed language, not the admin UI language', async () => {
    api.getById.mockResolvedValue({
      success: true,
      data: { ...draft, caseStudyChallengePl: 'Polskie wyzwanie' },
    });
    renderPreview();
    await englishTitle();

    fireEvent.click(screen.getByRole('button', { name: 'PL' }));

    expect(screen.getByRole('button', { name: i18n.t('caseStudy.title', { lng: 'pl' }) })).toBeTruthy();
    expect(screen.queryByRole('button', { name: i18n.t('caseStudy.title', { lng: 'en' }) })).toBeNull();
  });

  it('labels a published post as a preview of the live page instead of a draft', async () => {
    api.getById.mockResolvedValue({ success: true, data: { ...draft, published: true } });
    renderPreview();
    await englishTitle();

    expect(screen.queryByText(i18n.t('admin.preview.draftBanner'))).toBeNull();
    expect(screen.getByText(i18n.t('admin.preview.publishedBanner'))).toBeTruthy();
  });

  it('offers a retry when the load fails and a not-found message for a missing post', async () => {
    api.getById.mockRejectedValueOnce(new Error('network'));
    renderPreview();
    expect(await screen.findByRole('button', { name: i18n.t('common.retry') })).toBeTruthy();
    cleanup();

    api.getById.mockRejectedValueOnce({ response: { status: 404 } });
    renderPreview();
    expect(await screen.findByRole('heading', { name: i18n.t('post.notFound') })).toBeTruthy();
  });
});

function renderAdminRoute(path: string, pattern: string, element: React.ReactElement) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path={pattern} element={element} />
          <Route path="/admin/posts" element={<div>posts list</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

// Opens in a new tab so the form (and any unsaved edits) stays where it is
function expectNewTabLink(link: HTMLElement, href: string) {
  expect(link.getAttribute('href')).toBe(href);
  expect(link.getAttribute('target')).toBe('_blank');
  expect(link.getAttribute('rel')).toContain('noopener');
}

describe('Preview buttons', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  const previewName = () => ({ name: i18n.t('admin.preview.button') });

  it('opens the draft preview in a new tab from a post list row', async () => {
    api.getAll.mockResolvedValue({ success: true, data: [{ ...draft, title: 'English draft title', categoryLabel: 'Personal' }] });
    renderAdminRoute('/admin/posts', '/admin/posts', <AdminPosts />);

    expectNewTabLink(await screen.findByRole('link', previewName()), '/admin/posts/p1/preview');
  });

  it('offers the preview in the post form only for an existing post', async () => {
    api.getById.mockResolvedValue({ success: true, data: draft });
    renderAdminRoute('/admin/posts/p1/edit', '/admin/posts/:id/edit', <AdminPostForm />);
    expectNewTabLink(await screen.findByRole('link', previewName()), '/admin/posts/p1/preview');
    cleanup();

    renderAdminRoute('/admin/posts/new', '/admin/posts/new', <AdminPostForm />);
    await screen.findByRole('button', { name: i18n.t('common.save') });
    expect(screen.queryByRole('link', previewName())).toBeNull();
  });
});

describe('AdminPostForm unsaved-changes baseline', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  beforeEach(() => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
  });
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    vi.clearAllMocks();
  });

  // Content the editor normalises on load (an empty paragraph stored with an empty content array)
  const storedContent = {
    type: 'doc',
    content: [
      { type: 'heading', attrs: { level: 2 }, content: [{ type: 'text', text: 'Heading' }] },
      { type: 'paragraph', content: [{ type: 'text', marks: [{ type: 'bold' }], text: 'Bold text' }] },
      { type: 'paragraph', content: [] },
      {
        type: 'bulletList',
        content: [{ type: 'listItem', content: [{ type: 'paragraph', content: [{ type: 'text', text: 'Item' }] }] }],
      },
    ],
  };

  it('does not ask for confirmation on Cancel right after opening an existing post', async () => {
    api.getById.mockResolvedValue({
      success: true,
      data: { ...draft, contentEn: storedContent, contentPl: storedContent },
    });
    renderAdminRoute('/admin/posts/p1/edit', '/admin/posts/:id/edit', <AdminPostForm />);
    await screen.findByDisplayValue('English draft title');
    await screen.findAllByText('Bold text');
    // Let any editor update triggered by the initial content settle before Cancel
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 50));
    });

    fireEvent.click(screen.getByRole('button', { name: i18n.t('common.cancel') }));

    expect(window.confirm).not.toHaveBeenCalled();
    expect(screen.getByText('posts list')).toBeTruthy();
  });
});
