import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MediaType, type Media } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import MediaManager from './MediaManager';

const api = vi.hoisted(() => ({
  createYouTube: vi.fn(),
  delete: vi.fn(),
  upload: vi.fn(),
  reorder: vi.fn(),
}));
vi.mock('@/services/api', () => ({ adminMediaApi: api }));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

// jsdom has no layout, so the drag is simulated by calling the handler the manager hands to DndContext
const dnd = vi.hoisted(() => ({ onDragEnd: null as null | ((event: unknown) => void) }));
vi.mock('@dnd-kit/core', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@dnd-kit/core')>();
  return {
    ...actual,
    DndContext: ({ children, onDragEnd }: { children: React.ReactNode; onDragEnd: (event: unknown) => void }) => {
      dnd.onDragEnd = onDragEnd;
      return <>{children}</>;
    },
  };
});

const item: Media = {
  id: 'm1',
  postId: 'p1',
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
};

// The manager sits inside the post <form>, so any submit button or Enter would save the post
function renderInsideForm(media: Media[] = [item]) {
  const onSubmit = vi.fn((e: React.FormEvent) => e.preventDefault());
  const onMediaChange = vi.fn();
  const client = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
  const invalidate = vi.spyOn(client, 'invalidateQueries');
  const view = render(
    <QueryClientProvider client={client}>
      <form onSubmit={onSubmit}>
        <MediaManager postId="p1" media={media} onMediaChange={onMediaChange} />
      </form>
    </QueryClientProvider>
  );
  return { onSubmit, onMediaChange, view, invalidate };
}

describe('MediaManager inside a form', () => {
  beforeEach(() => {
    api.createYouTube.mockResolvedValue({ success: true, data: { ...item, id: 'yt', type: MediaType.YOUTUBE } });
    api.delete.mockResolvedValue({ success: true });
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('renders every button with type="button"', () => {
    const { view } = renderInsideForm();
    const buttons = Array.from(view.container.querySelectorAll('button'));
    expect(buttons.length).toBeGreaterThanOrEqual(2);
    expect(buttons.filter((b) => b.getAttribute('type') !== 'button')).toEqual([]);
  });

  it('deleting an item does not submit the surrounding form', async () => {
    const { onSubmit, view } = renderInsideForm();
    const remove = view.container.querySelector('button[class*="bg-red-500"]') as HTMLButtonElement;
    fireEvent.click(remove);
    await waitFor(() => expect(api.delete).toHaveBeenCalled());
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('Enter in the YouTube field adds the link and does not submit the form', async () => {
    const { onSubmit, onMediaChange } = renderInsideForm([]);
    const field = screen.getByPlaceholderText(/youtube/i);
    fireEvent.change(field, { target: { value: 'https://youtu.be/abc' } });
    const notPrevented = fireEvent.keyDown(field, { key: 'Enter' });
    expect(notPrevented).toBe(false);
    await waitFor(() => expect(api.createYouTube).toHaveBeenCalledWith('https://youtu.be/abc', 'p1'));
    await waitFor(() => expect(onMediaChange).toHaveBeenCalled());
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('Enter in an empty YouTube field adds nothing', () => {
    renderInsideForm([]);
    const field = screen.getByPlaceholderText(/youtube/i);
    fireEvent.keyDown(field, { key: 'Enter' });
    expect(api.createYouTube).not.toHaveBeenCalled();
  });

  it('restores the previous order when saving a reorder fails', async () => {
    api.reorder.mockRejectedValue(new Error('boom'));
    const second = { ...item, id: 'm2', displayOrder: 1 };
    const { onMediaChange } = renderInsideForm([item, second]);

    dnd.onDragEnd?.({ active: { id: 'm1' }, over: { id: 'm2' } });

    await waitFor(() => expect(api.reorder).toHaveBeenCalled());
    await waitFor(() => expect(onMediaChange).toHaveBeenCalledTimes(2));
    expect(onMediaChange.mock.calls[0][0].map((m: Media) => m.id)).toEqual(['m2', 'm1']);
    expect(onMediaChange.mock.calls[1][0].map((m: Media) => m.id)).toEqual(['m1', 'm2']);
  });

  describe('cache invalidation', () => {
    const expectPostCachesRefreshed = (invalidate: { mock: { calls: unknown[][] } }) => {
      const keys = invalidate.mock.calls.map((call) => (call[0] as { queryKey: unknown }).queryKey);
      expect(keys).toContainEqual(queryKeys.admin.post('p1'));
      expect(keys).toContainEqual(queryKeys.prefix.post());
      expect(keys).toContainEqual(queryKeys.prefix.posts());
    };

    it('refreshes the post caches after a delete', async () => {
      const { invalidate, view } = renderInsideForm();
      fireEvent.click(view.container.querySelector('button[class*="bg-red-500"]') as HTMLButtonElement);
      await waitFor(() => expect(api.delete).toHaveBeenCalled());
      await waitFor(() => expectPostCachesRefreshed(invalidate));
    });

    it('refreshes the post caches after adding a YouTube video', async () => {
      const { invalidate } = renderInsideForm([]);
      const field = screen.getByPlaceholderText(/youtube/i);
      fireEvent.change(field, { target: { value: 'https://youtu.be/abc' } });
      fireEvent.keyDown(field, { key: 'Enter' });
      await waitFor(() => expectPostCachesRefreshed(invalidate));
    });

    it('refreshes the post caches after an upload', async () => {
      api.upload.mockResolvedValue({ success: true, data: { ...item, id: 'up' } });
      const { invalidate, view } = renderInsideForm([]);
      const input = view.container.querySelector('input[type="file"]') as HTMLInputElement;
      fireEvent.change(input, { target: { files: [new File(['x'], 'x.png', { type: 'image/png' })] } });
      await waitFor(() => expect(api.upload).toHaveBeenCalled());
      await waitFor(() => expectPostCachesRefreshed(invalidate));
    });

    it('refreshes the post caches after a successful reorder', async () => {
      api.reorder.mockResolvedValue({ success: true });
      const { invalidate } = renderInsideForm([item, { ...item, id: 'm2', displayOrder: 1 }]);
      dnd.onDragEnd?.({ active: { id: 'm1' }, over: { id: 'm2' } });
      await waitFor(() => expectPostCachesRefreshed(invalidate));
    });
  });
});
