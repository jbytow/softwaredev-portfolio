import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import i18n from '@/i18n/index';
import { MediaType, type Media } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import AdminMedia from './AdminMedia';

const api = vi.hoisted(() => ({ getAll: vi.fn(), upload: vi.fn(), delete: vi.fn() }));
vi.mock('@/services/api', () => ({ adminMediaApi: api }));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

const item: Media = {
  id: 'm1',
  postId: null,
  type: MediaType.IMAGE,
  filename: 'a.png',
  originalName: 'a.png',
  mimeType: 'image/png',
  size: 10,
  url: '/media/a.png',
  altText: '',
  altTextEn: '',
  altTextPl: '',
  displayOrder: 0,
  videoUrl: null,
  createdAt: '2026-01-01T00:00:00Z',
} as Media;

const page = (number: number, totalPages: number) => ({
  data: { content: [item], totalPages, number, totalElements: totalPages * 100 },
});

function renderMedia() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const invalidate = vi.spyOn(client, 'invalidateQueries');
  render(
    <QueryClientProvider client={client}>
      <AdminMedia />
    </QueryClientProvider>
  );
  return { invalidate };
}

describe('AdminMedia', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('loads further pages of 100 through the page buttons', async () => {
    api.getAll.mockImplementation((p: number) => Promise.resolve(page(p, 3)));
    renderMedia();
    await screen.findByText('1 / 3');
    expect(api.getAll).toHaveBeenCalledWith(0, 100);

    fireEvent.click(screen.getByRole('button', { name: 'Next' }));

    await screen.findByText('2 / 3');
    expect(api.getAll).toHaveBeenCalledWith(1, 100);
  });

  it('refreshes every query that embeds media after a delete', async () => {
    api.getAll.mockResolvedValue(page(0, 1));
    api.delete.mockResolvedValue({});
    const { invalidate } = renderMedia();
    await screen.findByAltText('a.png');

    fireEvent.click(document.querySelector('.group button') as HTMLElement);
    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));

    await waitFor(() => expect(api.delete).toHaveBeenCalledWith('m1'));
    await waitFor(() => {
      const keys = invalidate.mock.calls.map(([filters]) => JSON.stringify(filters?.queryKey));
      expect(keys).toContain(JSON.stringify(queryKeys.prefix.posts()));
      expect(keys).toContain(JSON.stringify(queryKeys.prefix.settings()));
      expect(keys).toContain(JSON.stringify(queryKeys.admin.mediaAll()));
    });
  });
});
