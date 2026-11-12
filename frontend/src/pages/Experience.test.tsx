import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HelmetProvider } from 'react-helmet-async';
import i18n from '@/i18n/index';
import Experience from './Experience';

const getAll = vi.hoisted(() => vi.fn());
vi.mock('@/services/api', () => ({
  experiencesApi: { getAll },
}));
vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: 'en' }),
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));

function renderExperience() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <HelmetProvider>
      <QueryClientProvider client={client}>
        <Experience />
      </QueryClientProvider>
    </HelmetProvider>
  );
}

const experience = (over: object) => ({
  id: 'e1',
  title: 'Developer',
  company: 'Acme',
  description: null,
  achievements: [],
  displayOrder: 1,
  ...over,
});

describe('Experience', () => {
  const originalTz = process.env.TZ;

  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    getAll.mockReset();
    process.env.TZ = originalTz;
  });

  it('keeps the calendar month of a YYYY-MM-DD date west of UTC', async () => {
    process.env.TZ = 'America/Los_Angeles';
    getAll.mockResolvedValueOnce({
      success: true,
      data: [experience({ startDate: '2024-03-01', endDate: '2024-05-01' })],
    });
    renderExperience();

    // new Date('2024-03-01') is UTC midnight, which is 29 Feb in Los Angeles and would show "feb 2024"
    expect(await screen.findByText(/mar 2024 – may 2024/)).toBeTruthy();
    expect(screen.getByText(`3 ${i18n.t('experience.monthsShort')}`)).toBeTruthy();
  });

  it('sorts entries by local start date, newest first', async () => {
    getAll.mockResolvedValueOnce({
      success: true,
      data: [
        experience({ id: 'old', title: 'Older role', startDate: '2020-01-01', endDate: '2021-01-01' }),
        experience({ id: 'new', title: 'Newer role', startDate: '2022-01-01', endDate: null }),
      ],
    });
    renderExperience();

    const headings = await screen.findAllByRole('heading', { level: 3 });
    expect(headings.map((h) => h.textContent)).toEqual(['Newer role', 'Older role']);
  });

  it('shows the load error with retry, not the empty text, when the API fails', async () => {
    getAll.mockRejectedValueOnce(new Error('network'));
    getAll.mockResolvedValueOnce({ success: true, data: [experience({ startDate: '2024-03-01', endDate: null })] });
    renderExperience();

    const retry = await screen.findByRole('button', { name: i18n.t('common.retry') });
    expect(screen.getByText(i18n.t('common.loadError'))).toBeTruthy();
    expect(screen.queryByText(i18n.t('experience.empty'))).toBeNull();

    fireEvent.click(retry);
    expect(await screen.findByText('Developer')).toBeTruthy();
  });

  it('shows the translated empty text when there are no entries', async () => {
    getAll.mockResolvedValueOnce({ success: true, data: [] });
    renderExperience();

    expect(await screen.findByText(i18n.t('experience.empty'))).toBeTruthy();
    expect(i18n.t('experience.empty')).not.toBe('experience.empty');
  });
});
