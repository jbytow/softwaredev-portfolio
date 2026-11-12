import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MediaType, type Media } from '@/types';
import MediaManager from './MediaManager';

const api = vi.hoisted(() => ({
  createYouTube: vi.fn(),
  delete: vi.fn(),
  upload: vi.fn(),
  reorder: vi.fn(),
}));
vi.mock('@/services/api', () => ({ adminMediaApi: api }));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

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
  const view = render(
    <QueryClientProvider client={client}>
      <form onSubmit={onSubmit}>
        <MediaManager postId="p1" media={media} onMediaChange={onMediaChange} />
      </form>
    </QueryClientProvider>
  );
  return { onSubmit, onMediaChange, view };
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
});
