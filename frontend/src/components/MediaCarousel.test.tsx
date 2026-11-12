import { afterEach, beforeAll, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import i18n from '@/i18n/index';
import MediaCarousel from '@/components/MediaCarousel';
import { Media, MediaType } from '@/types';

function image(n: number): Media {
  return {
    id: `m${n}`,
    postId: null,
    type: MediaType.IMAGE,
    filename: null,
    originalName: null,
    mimeType: null,
    size: null,
    url: `/img/${n}.jpg`,
    altText: `Image ${n}`,
    altTextEn: `Image ${n}`,
    altTextPl: `Obraz ${n}`,
    displayOrder: n,
    videoUrl: null,
    createdAt: '2026-01-01T00:00:00Z',
  };
}

function youtube(id: string, url = `https://www.youtube.com/embed/${id}`): Media {
  return { ...image(9), id: `yt-${id}`, type: MediaType.YOUTUBE, url, altText: 'Campaign film' };
}

describe('MediaCarousel', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(cleanup);

  it('keeps rendering when the media list shrinks below the current slide', () => {
    const { rerender } = render(<MediaCarousel media={[image(1), image(2), image(3)]} />);
    fireEvent.click(screen.getByRole('button', { name: 'Go to slide 3' }));

    expect(() => rerender(<MediaCarousel media={[image(1)]} />)).not.toThrow();
    expect(screen.getByAltText('Image 1')).toBeTruthy();
  });

  it('shows the last remaining slide after the list shrinks to two items', () => {
    const { rerender } = render(<MediaCarousel media={[image(1), image(2), image(3)]} />);
    fireEvent.click(screen.getByRole('button', { name: 'Go to slide 3' }));
    rerender(<MediaCarousel media={[image(1), image(2)]} />);

    expect(screen.getAllByAltText('Image 2').length).toBeGreaterThan(0);
  });

  it('labels the controls in the active language', async () => {
    await i18n.changeLanguage('pl');
    render(<MediaCarousel media={[image(1), image(2)]} />);

    expect(screen.getByRole('button', { name: 'Przejdź do slajdu 2' })).toBeTruthy();
    expect(screen.getAllByRole('button', { name: 'Poprzedni' }).length).toBe(1);
    await i18n.changeLanguage('en');
  });

  describe('YouTube facade', () => {
    const id = 'dQw4w9WgXcQ';

    it('renders a thumbnail button and no iframe until the visitor clicks', () => {
      const { container } = render(<MediaCarousel media={[youtube(id)]} />);

      expect(container.querySelector('iframe')).toBeNull();
      const button = screen.getByRole('button', { name: 'Play video' });
      expect(button.tagName).toBe('BUTTON');
      expect(button.getAttribute('type')).toBe('button');
      expect(button.querySelector('img')?.getAttribute('src')).toBe(`https://i.ytimg.com/vi/${id}/hqdefault.jpg`);
    });

    it('loads the privacy-enhanced player with autoplay after the click', () => {
      const { container } = render(<MediaCarousel media={[youtube(id)]} />);
      fireEvent.click(screen.getByRole('button', { name: 'Play video' }));

      const iframe = container.querySelector('iframe')!;
      expect(iframe.getAttribute('src')).toBe(`https://www.youtube-nocookie.com/embed/${id}?autoplay=1`);
      expect(iframe.getAttribute('title')).toBe('Campaign film');
      expect(iframe.getAttribute('allow')).toContain('autoplay');
      expect(screen.queryByRole('button', { name: 'Play video' })).toBeNull();
    });

    it.each([
      ['an embed URL', `https://www.youtube.com/embed/${id}`],
      ['a nocookie embed URL', `https://www.youtube-nocookie.com/embed/${id}`],
      ['a watch URL', `https://www.youtube.com/watch?si=abc&v=${id}`],
      ['a short link', `https://youtu.be/${id}`],
    ])('takes the video id from %s', (_label, url) => {
      const { container } = render(<MediaCarousel media={[youtube(id, url)]} />);
      expect(container.querySelector('img')?.getAttribute('src')).toBe(`https://i.ytimg.com/vi/${id}/hqdefault.jpg`);
    });

    it('never loads a player from a URL it cannot read a video id from', () => {
      const { container } = render(<MediaCarousel media={[youtube(id, 'https://evil.example/embed/x')]} />);
      expect(container.querySelector('iframe')).toBeNull();
      expect(container.querySelector('img')).toBeNull();
    });

    it('does not preload players for neighbouring slides and resets the facade on slide change', () => {
      const { container } = render(<MediaCarousel media={[youtube(id), image(2), image(3)]} />);
      expect(container.querySelector('iframe')).toBeNull();

      fireEvent.click(screen.getByRole('button', { name: 'Play video' }));
      expect(container.querySelector('iframe')).not.toBeNull();

      fireEvent.click(screen.getByRole('button', { name: 'Go to slide 2' }));
      fireEvent.click(screen.getByRole('button', { name: 'Go to slide 1' }));
      expect(container.querySelector('iframe')).toBeNull();
      expect(screen.getByRole('button', { name: 'Play video' })).toBeTruthy();
    });

    it('labels the play button in the active language', async () => {
      await i18n.changeLanguage('pl');
      render(<MediaCarousel media={[youtube(id)]} />);
      expect(screen.getByRole('button', { name: 'Odtwórz film' })).toBeTruthy();
      await i18n.changeLanguage('en');
    });
  });
});
