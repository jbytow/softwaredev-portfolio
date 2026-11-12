import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { HelmetProvider } from 'react-helmet-async';
import i18n from '@/i18n/index';
import Contact from '@/pages/Contact';
import { contactApi } from '@/services/api';

const siteSettings = vi.hoisted(() => ({
  current: {} as { data?: { data: object }; isPending: boolean; isError: boolean; refetch: () => void },
}));
vi.mock('@/services/api', () => ({
  contactApi: { submit: vi.fn() },
}));
vi.mock('@/lib/useSiteSettings', () => ({
  useSiteSettings: () => siteSettings.current,
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

function renderContact() {
  return render(
    <HelmetProvider>
      <Contact />
    </HelmetProvider>
  );
}

describe('Contact', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  beforeEach(() => {
    siteSettings.current = { data: { data: { socialLinks: {} } }, isPending: false, isError: false, refetch: vi.fn() };
    vi.mocked(contactApi.submit).mockResolvedValue({ success: true, message: '', data: undefined });
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  describe('honeypot', () => {
    it('submits an empty website field together with the visible fields', async () => {
      renderContact();
      fireEvent.change(screen.getByLabelText(i18n.t('contact.form.name')), { target: { value: 'Ada' } });
      fireEvent.change(screen.getByLabelText(i18n.t('contact.form.email')), { target: { value: 'ada@example.com' } });
      fireEvent.change(screen.getByLabelText(i18n.t('contact.form.subject')), { target: { value: 'Hello' } });
      fireEvent.change(screen.getByLabelText(i18n.t('contact.form.message')), { target: { value: 'A message' } });
      fireEvent.click(screen.getByRole('button', { name: i18n.t('contact.form.send') }));

      await waitFor(() => expect(contactApi.submit).toHaveBeenCalledTimes(1));
      expect(contactApi.submit).toHaveBeenCalledWith({
        name: 'Ada',
        email: 'ada@example.com',
        subject: 'Hello',
        message: 'A message',
        website: '',
      });
    });

    it('keeps the trap out of keyboard and assistive technology reach but still in the DOM', () => {
      const { container } = renderContact();
      const trap = container.querySelector<HTMLInputElement>('input[name="website"]');

      expect(trap).not.toBeNull();
      expect(trap!.tabIndex).toBe(-1);
      expect(trap!.getAttribute('autocomplete')).toBe('off');
      const wrapper = trap!.closest<HTMLElement>('[aria-hidden="true"]');
      expect(wrapper).not.toBeNull();
      // display:none is skipped by many bots, so the field is moved off-screen instead
      expect(wrapper!.style.position).toBe('absolute');
      expect(wrapper!.style.left).toBe('-9999px');
      expect(wrapper!.style.display).not.toBe('none');
    });

    it('labels the trap in the active language', async () => {
      const { container } = renderContact();
      expect(i18n.t('contact.honeypot')).not.toBe('contact.honeypot');
      expect(container.textContent).toContain(i18n.t('contact.honeypot'));
      await i18n.changeLanguage('pl');
      expect(container.textContent).toContain(i18n.t('contact.honeypot'));
      await i18n.changeLanguage('en');
    });
  });

  it('limits the field lengths to what the backend accepts', () => {
    renderContact();
    expect(screen.getByLabelText(i18n.t('contact.form.name')).getAttribute('maxlength')).toBe('100');
    expect(screen.getByLabelText(i18n.t('contact.form.subject')).getAttribute('maxlength')).toBe('200');
    expect(screen.getByLabelText(i18n.t('contact.form.message')).getAttribute('maxlength')).toBe('5000');
  });

  it('skips social links with an empty url and hides the block when none is left', () => {
    siteSettings.current = {
      ...siteSettings.current,
      data: { data: { socialLinks: { github: 'https://github.com/jbytow', linkedin: '', facebook: '  ' } } },
    };
    renderContact();
    expect(screen.getByRole('link', { name: 'github' })).toBeTruthy();
    expect(screen.queryByRole('link', { name: 'linkedin' })).toBeNull();
    expect(screen.queryByRole('link', { name: 'facebook' })).toBeNull();

    cleanup();
    siteSettings.current = { ...siteSettings.current, data: { data: { socialLinks: { linkedin: '' } } } };
    renderContact();
    expect(screen.queryByText(i18n.t('contact.info.social'))).toBeNull();
  });

  it('shows a load error with retry in the info column when the settings fail, and keeps the form', () => {
    const refetch = vi.fn();
    siteSettings.current = { data: undefined, isPending: false, isError: true, refetch };
    renderContact();

    expect(screen.getByLabelText(i18n.t('contact.form.name'))).toBeTruthy();
    expect(screen.getByText(i18n.t('common.loadError'))).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: i18n.t('common.retry') }));
    expect(refetch).toHaveBeenCalledTimes(1);
  });
});
