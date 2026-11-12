import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import i18n from '@/i18n/index';
import AdminPostForm from './AdminPostForm';

const api = vi.hoisted(() => ({ getById: vi.fn(), create: vi.fn(), update: vi.fn() }));
vi.mock('@/services/api', () => ({ adminPostsApi: api }));
vi.mock('@/admin/components/MediaManager', () => ({ default: () => null }));
vi.mock('@/components/RichTextEditor', () => ({ default: () => null }));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

function post(overrides: Record<string, unknown> = {}) {
  return {
    id: 'p1',
    category: 'PERSONAL_PROJECT',
    titleEn: 'Title',
    titlePl: 'Tytul',
    slug: 'stored-slug',
    media: [],
    hashtags: [],
    published: false,
    ...overrides,
  };
}

function renderForm(path: string) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/admin/posts/new" element={<AdminPostForm />} />
          <Route path="/admin/posts/:id/edit" element={<AdminPostForm />} />
          <Route path="/admin/posts" element={<div>list</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

const slugInput = () => document.querySelector('input[name="slug"]') as HTMLInputElement;
const submit = () => fireEvent.submit(document.querySelector('form') as HTMLFormElement);

describe('AdminPostForm', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  beforeEach(() => {
    api.create.mockResolvedValue({ success: true });
    api.update.mockResolvedValue({ success: true });
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  describe('slug on save', () => {
    it('omits a blank slug on create so the backend generates one', async () => {
      renderForm('/admin/posts/new');
      submit();
      await waitFor(() => expect(api.create).toHaveBeenCalledTimes(1));
      expect(api.create.mock.calls[0][0].slug).toBeUndefined();
    });

    it('omits a cleared slug on update so the stored one is kept', async () => {
      api.getById.mockResolvedValue({ success: true, data: post() });
      renderForm('/admin/posts/p1/edit');
      await waitFor(() => expect(slugInput().value).toBe('stored-slug'));
      fireEvent.change(slugInput(), { target: { value: '   ' } });
      submit();
      await waitFor(() => expect(api.update).toHaveBeenCalledTimes(1));
      expect(api.update.mock.calls[0][1].slug).toBeUndefined();
    });

    it('sends a typed slug trimmed', async () => {
      renderForm('/admin/posts/new');
      fireEvent.change(slugInput(), { target: { value: ' my-slug ' } });
      submit();
      await waitFor(() => expect(api.create).toHaveBeenCalledTimes(1));
      expect(api.create.mock.calls[0][0].slug).toBe('my-slug');
    });
  });

  describe('case study badge', () => {
    const badge = () => i18n.t('admin.posts.caseStudy.hasContent');

    it.each([
      'caseStudyChallengeEn',
      'caseStudySolutionEn',
      'caseStudyResultsEn',
      'caseStudyTestimonialEn',
      'caseStudyChallengePl',
      'caseStudySolutionPl',
      'caseStudyResultsPl',
      'caseStudyTestimonialPl',
    ])('shows it when only %s has text', async (field) => {
      api.getById.mockResolvedValue({ success: true, data: post({ [field]: 'text' }) });
      renderForm('/admin/posts/p1/edit');
      expect(await screen.findByText(badge())).toBeTruthy();
    });

    it('hides it when the case study fields are empty or whitespace', async () => {
      api.getById.mockResolvedValue({
        success: true,
        data: post({ caseStudyChallengeEn: '   ', caseStudyChallengePl: '' }),
      });
      renderForm('/admin/posts/p1/edit');
      await waitFor(() => expect(slugInput().value).toBe('stored-slug'));
      expect(screen.queryByText(badge())).toBeNull();
    });
  });
});
