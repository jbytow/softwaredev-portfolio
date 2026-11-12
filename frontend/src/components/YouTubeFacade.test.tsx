import { afterEach, beforeAll, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import i18n from '@/i18n/index';
import YouTubeFacade from '@/components/YouTubeFacade';

const id = 'dQw4w9WgXcQ';

describe('YouTubeFacade', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(cleanup);

  it('shows a thumbnail button and no iframe before the click', () => {
    const { container } = render(<YouTubeFacade url={`https://www.youtube.com/embed/${id}`} title="Film" />);

    expect(container.querySelector('iframe')).toBeNull();
    const button = screen.getByRole('button', { name: 'Play video' });
    expect(button.getAttribute('type')).toBe('button');
    expect(button.querySelector('img')?.getAttribute('src')).toBe(`https://i.ytimg.com/vi/${id}/hqdefault.jpg`);
  });

  it('loads the youtube-nocookie player with autoplay after the click', () => {
    const { container } = render(<YouTubeFacade url={`https://youtu.be/${id}`} title="Film" />);
    fireEvent.click(screen.getByRole('button', { name: 'Play video' }));

    const iframe = container.querySelector('iframe')!;
    expect(iframe.getAttribute('src')).toBe(`https://www.youtube-nocookie.com/embed/${id}?autoplay=1`);
    expect(iframe.getAttribute('title')).toBe('Film');
  });

  it('renders nothing for a URL without a readable video id', () => {
    const { container } = render(<YouTubeFacade url="https://evil.example/embed/x" title="Film" />);

    expect(container.innerHTML).toBe('');
  });
});
