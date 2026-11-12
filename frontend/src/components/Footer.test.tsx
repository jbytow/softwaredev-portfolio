import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import i18n from '@/i18n/index';
import Footer from './Footer';

const settings = vi.hoisted(() => ({ current: {} as Record<string, unknown> }));
vi.mock('@/lib/useSiteSettings', () => ({
  useSiteSettings: () => ({ data: { data: settings.current } }),
}));
vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: 'en' }),
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));

function renderFooter() {
  render(
    <MemoryRouter>
      <Footer />
    </MemoryRouter>
  );
}

describe('Footer social links', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    settings.current = {};
  });

  it('renders only links that have a non-blank url', () => {
    settings.current = {
      socialLinks: { github: 'https://github.com/jbytow', linkedin: '', facebook: '   ', instagram: null },
    };
    renderFooter();

    expect(screen.getByRole('link', { name: 'github' }).getAttribute('href')).toBe('https://github.com/jbytow');
    for (const empty of ['linkedin', 'facebook', 'instagram']) {
      expect(screen.queryByRole('link', { name: empty })).toBeNull();
    }
  });

  it('falls back to the settings email when the email social link is blank', () => {
    settings.current = { email: 'me@example.com', socialLinks: { email: '' } };
    renderFooter();

    const mail = screen.getByRole('link', { name: i18n.t('contact.info.email') });
    expect(mail.getAttribute('href')).toBe('mailto:me@example.com');
  });
});
