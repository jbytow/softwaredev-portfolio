import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import i18n from '@/i18n/index';
import SessionExpiredModal from './SessionExpiredModal';

describe('SessionExpiredModal', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('renders nothing while closed', () => {
    render(<SessionExpiredModal open={false} onClose={vi.fn()} />);
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('shows the translated texts in an accessible modal dialog', () => {
    render(<SessionExpiredModal open onClose={vi.fn()} />);
    const dialog = screen.getByRole('dialog');
    expect(dialog.getAttribute('aria-modal')).toBe('true');
    expect(i18n.t('admin.session.expiredTitle')).not.toBe('admin.session.expiredTitle');
    expect(screen.getByText(i18n.t('admin.session.expiredTitle'))).toBeTruthy();
    expect(screen.getByText(i18n.t('admin.session.expiredBody'))).toBeTruthy();
  });

  it('moves focus into the dialog', () => {
    render(<SessionExpiredModal open onClose={vi.fn()} />);
    expect(screen.getByRole('dialog').contains(document.activeElement)).toBe(true);
  });

  it('closes on Escape', () => {
    const onClose = vi.fn();
    render(<SessionExpiredModal open onClose={onClose} />);
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('does not listen for Escape while closed', () => {
    const onClose = vi.fn();
    render(<SessionExpiredModal open={false} onClose={onClose} />);
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(onClose).not.toHaveBeenCalled();
  });

  it('closes through the secondary button', () => {
    const onClose = vi.fn();
    render(<SessionExpiredModal open onClose={onClose} />);
    fireEvent.click(screen.getByRole('button', { name: i18n.t('admin.session.close') }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('opens the login page in a new tab without handing over window.opener', () => {
    const open = vi.spyOn(window, 'open').mockReturnValue(null);
    render(<SessionExpiredModal open onClose={vi.fn()} />);
    fireEvent.click(screen.getByRole('button', { name: i18n.t('admin.session.openLogin') }));
    expect(open).toHaveBeenCalledWith('/admin/login', '_blank', 'noopener');
  });
});
