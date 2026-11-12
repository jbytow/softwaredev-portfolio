import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import i18n from '@/i18n/index';
import ImageLightbox from '@/components/ImageLightbox';

function Harness({ isOpen, onClose = () => {} }: { isOpen: boolean; onClose?: () => void }) {
  return (
    <>
      <button>opener</button>
      <ImageLightbox src="/a.jpg" alt="Big picture" isOpen={isOpen} onClose={onClose} />
    </>
  );
}

describe('ImageLightbox', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(cleanup);

  it('is exposed as a modal dialog named after the image', () => {
    render(<Harness isOpen />);

    const dialog = screen.getByRole('dialog', { name: 'Big picture' });
    expect(dialog.getAttribute('aria-modal')).toBe('true');
  });

  it('moves focus to the close button and keeps it there on Tab', () => {
    render(<Harness isOpen />);
    const close = screen.getByRole('button', { name: 'Close' });

    expect(document.activeElement).toBe(close);
    const tabbed = fireEvent.keyDown(document, { key: 'Tab' });
    expect(tabbed).toBe(false);
    expect(document.activeElement).toBe(close);
  });

  it('restores focus to the opener when it closes', () => {
    const { rerender } = render(<Harness isOpen={false} />);
    const opener = screen.getByRole('button', { name: 'opener' });
    opener.focus();

    rerender(<Harness isOpen />);
    expect(document.activeElement).not.toBe(opener);
    rerender(<Harness isOpen={false} />);

    expect(document.activeElement).toBe(opener);
  });

  it('closes on Escape and labels the close button in Polish', async () => {
    const onClose = vi.fn();
    await i18n.changeLanguage('pl');
    render(<Harness isOpen onClose={onClose} />);

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(onClose).toHaveBeenCalledTimes(1);
    expect(screen.getByRole('button', { name: 'Zamknij' })).toBeTruthy();
    await i18n.changeLanguage('en');
  });
});
