import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HelmetProvider } from 'react-helmet-async';
import i18n from '@/i18n/index';
import About from './About';

const getSettings = vi.hoisted(() => vi.fn());
const getInterests = vi.hoisted(() => vi.fn());
const getAchievements = vi.hoisted(() => vi.fn());
const getRpgStats = vi.hoisted(() => vi.fn());
vi.mock('@/services/api', () => ({
  settingsApi: { get: getSettings },
  interestsApi: { getAll: getInterests },
  achievementsApi: { getAll: getAchievements },
  rpgStatsApi: { getAll: getRpgStats },
}));
vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: 'en' }),
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));
vi.mock('@/components/LoadingSpinner', () => ({
  default: () => <div role="status">loading</div>,
}));

const settingsOk = {
  success: true,
  data: { aboutText: 'I write backends.', aboutTags: ['Java'], profileImage: null },
};
const empty = { success: true, data: [] };

function renderAbout() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <HelmetProvider>
      <QueryClientProvider client={client}>
        <About />
      </QueryClientProvider>
    </HelmetProvider>
  );
}

describe('About', () => {
  beforeAll(async () => {
    // jsdom has no IntersectionObserver, which framer-motion whileInView needs
    vi.stubGlobal(
      'IntersectionObserver',
      class {
        observe() {}
        unobserve() {}
        disconnect() {}
      }
    );
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    localStorage.clear();
    for (const mock of [getSettings, getInterests, getAchievements, getRpgStats]) mock.mockReset();
  });

  function answerWith(overrides: Partial<Record<'interests' | 'achievements' | 'rpgStats', unknown>> = {}) {
    getInterests.mockResolvedValue(overrides.interests ?? empty);
    getAchievements.mockResolvedValue(overrides.achievements ?? empty);
    getRpgStats.mockResolvedValue(overrides.rpgStats ?? empty);
  }

  it('shows the load error with retry instead of the placeholder text when settings fail', async () => {
    getSettings.mockRejectedValueOnce(new Error('network'));
    getSettings.mockResolvedValueOnce(settingsOk);
    answerWith();
    renderAbout();

    const retry = await screen.findByRole('button', { name: i18n.t('common.retry') });
    expect(screen.getByText(i18n.t('common.loadError'))).toBeTruthy();
    expect(screen.queryByText(i18n.t('about.placeholder'))).toBeNull();

    fireEvent.click(retry);
    await waitFor(() => expect(getSettings).toHaveBeenCalledTimes(2));
    expect(await screen.findByText('I write backends.')).toBeTruthy();
    expect(screen.queryByText(i18n.t('common.loadError'))).toBeNull();
  });

  it('keeps the spinner while settings are pending', () => {
    getSettings.mockReturnValue(new Promise(() => {}));
    answerWith();
    renderAbout();

    expect(screen.getByRole('status')).toBeTruthy();
    expect(screen.queryByText(i18n.t('common.loadError'))).toBeNull();
  });

  it('renders the about text and tags when settings load', async () => {
    getSettings.mockResolvedValue(settingsOk);
    answerWith();
    renderAbout();

    expect(await screen.findByText('I write backends.')).toBeTruthy();
    expect(screen.getByText('Java')).toBeTruthy();
    expect(screen.queryByText(i18n.t('common.loadError'))).toBeNull();
  });

  it('strips scripts and event handlers from the about text', async () => {
    getSettings.mockResolvedValue({
      success: true,
      data: { aboutText: '<p onclick="alert(1)">Safe</p><script>alert(2)</script><img src=x onerror="alert(3)">', aboutTags: [] },
    });
    answerWith();
    renderAbout();
    const container = document.body;

    expect(await screen.findByText('Safe')).toBeTruthy();
    expect(container.querySelector('script')).toBeNull();
    expect(container.querySelector('img[onerror]')).toBeNull();
    expect(container.querySelector('[onclick]')).toBeNull();
  });

  it('shows the translated placeholder when the about text is empty', async () => {
    getSettings.mockResolvedValue({ success: true, data: { aboutText: '', aboutTags: [] } });
    answerWith();
    renderAbout();

    expect(await screen.findByText(i18n.t('about.placeholder'))).toBeTruthy();
    expect(i18n.t('about.placeholder')).not.toBe('about.placeholder');
  });

  it('keeps the page and shows a local error with retry when the interests fail', async () => {
    getSettings.mockResolvedValue(settingsOk);
    answerWith();
    getInterests.mockReset();
    getInterests.mockRejectedValueOnce(new Error('network'));
    getInterests.mockResolvedValueOnce({
      success: true,
      data: [{ id: 'i1', title: 'Chess', image1: null, image2: null, image3: null }],
    });
    renderAbout();

    expect(await screen.findByText('I write backends.')).toBeTruthy();
    const retry = await screen.findByRole('button', { name: i18n.t('common.retry') });
    fireEvent.click(retry);
    expect(await screen.findByText('Chess')).toBeTruthy();
    expect(screen.queryByText(i18n.t('common.loadError'))).toBeNull();
  });

  it('shows a local error instead of hiding the RPG sections when their queries fail', async () => {
    localStorage.setItem('rpgMode', 'true');
    getSettings.mockResolvedValue(settingsOk);
    answerWith();
    getAchievements.mockReset();
    getRpgStats.mockReset();
    getAchievements.mockRejectedValue(new Error('network'));
    getRpgStats.mockRejectedValue(new Error('network'));
    renderAbout();

    expect(await screen.findAllByRole('button', { name: i18n.t('common.retry') })).toHaveLength(2);
    expect(screen.getAllByText(i18n.t('common.loadError'))).toHaveLength(2);
    expect(screen.getByText(i18n.t('about.rpg.classTitle'))).toBeTruthy();
  });
});
