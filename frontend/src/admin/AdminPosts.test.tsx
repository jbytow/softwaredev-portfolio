import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import i18n from '@/i18n/index';
import AdminPosts from './AdminPosts';

const api = vi.hoisted(() => ({ getAll: vi.fn(), togglePublish: vi.fn(), delete: vi.fn() }));
vi.mock('@/services/api', () => ({ adminPostsApi: api }));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

const post = {
  id: 'p1',
  title: 'First post',
  slug: 'first-post',
  category: 'PERSONAL_PROJECT',
  published: false,
  hashtags: [],
  createdAt: '2026-01-01T00:00:00Z',
};

function renderList() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <AdminPosts />
      </MemoryRouter>
    </QueryClientProvider>
  );
}

describe('AdminPosts', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('blocks the publish toggle while its request is in flight', async () => {
    api.getAll.mockResolvedValue({ success: true, data: [post] });
    let finish: (value: unknown) => void = () => {};
    api.togglePublish.mockReturnValue(new Promise((resolve) => (finish = resolve)));
    renderList();

    const toggle = (await screen.findByTitle('Publish')) as HTMLButtonElement;
    fireEvent.click(toggle);
    await waitFor(() => expect(toggle.disabled).toBe(true));
    fireEvent.click(toggle);
    expect(api.togglePublish).toHaveBeenCalledTimes(1);

    finish({ success: true });
    await waitFor(() => expect(toggle.disabled).toBe(false));
  });

  it('asks in a modal before deleting and deletes only after confirmation', async () => {
    api.getAll.mockResolvedValue({ success: true, data: [post] });
    api.delete.mockResolvedValue({ success: true });
    renderList();

    const remove = (await screen.findByText('First post')).closest('tr')!.querySelector('button[class*="red"]') as HTMLButtonElement;
    fireEvent.click(remove);
    expect(api.delete).not.toHaveBeenCalled();
    expect(screen.getByText('Are you sure you want to delete "First post"?')).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: i18n.t('common.delete') }));
    await waitFor(() => expect(api.delete).toHaveBeenCalledWith('p1'));
  });
});
