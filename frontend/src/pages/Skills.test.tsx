import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HelmetProvider } from 'react-helmet-async';
import i18n from '@/i18n/index';
import Skills from './Skills';

const getAllWithSkills = vi.hoisted(() => vi.fn());
vi.mock('@/services/api', () => ({
  skillCategoriesApi: { getAllWithSkills },
}));
vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: 'en' }),
}));
vi.mock('@/contexts/NerdModeContext', () => ({
  useNerdMode: () => ({ nerdMode: false }),
}));

function renderSkills() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <HelmetProvider>
      <QueryClientProvider client={client}>
        <Skills />
      </QueryClientProvider>
    </HelmetProvider>
  );
}

const INVENTED_SKILLS = ['Java', 'Spring Boot', 'PostgreSQL', 'React', 'Git', 'Docker'];

describe('Skills', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    getAllWithSkills.mockReset();
    localStorage.clear();
  });

  it('shows the load error with a retry button when the API fails, not an empty page', async () => {
    getAllWithSkills.mockRejectedValueOnce(new Error('network'));
    getAllWithSkills.mockResolvedValueOnce({ success: true, data: [] });
    renderSkills();

    const retry = await screen.findByRole('button', { name: i18n.t('common.retry') });
    expect(screen.getByText(i18n.t('common.loadError'))).toBeTruthy();
    expect(screen.queryByText(i18n.t('skills.empty'))).toBeNull();
    for (const invented of INVENTED_SKILLS) expect(screen.queryByText(invented)).toBeNull();

    fireEvent.click(retry);
    await waitFor(() => expect(getAllWithSkills).toHaveBeenCalledTimes(2));
    await waitFor(() => expect(screen.queryByText(i18n.t('common.loadError'))).toBeNull());
  });

  it.each([false, true])('shows the empty text and no invented skills when there are none (xp mode %s)', async (xp) => {
    localStorage.setItem('xpMode', String(xp));
    getAllWithSkills.mockResolvedValueOnce({
      success: true,
      data: [{ id: 'c1', name: 'Empty category', displayOrder: 1, skills: [] }],
    });
    renderSkills();

    expect(await screen.findByText(i18n.t('skills.empty'))).toBeTruthy();
    expect(i18n.t('skills.empty')).not.toBe('skills.empty');
    for (const invented of INVENTED_SKILLS) expect(screen.queryByText(invented)).toBeNull();
  });

  it('renders the categories when the API answers', async () => {
    getAllWithSkills.mockResolvedValueOnce({
      success: true,
      data: [
        {
          id: 'c1',
          name: 'Backend',
          displayOrder: 1,
          skills: [{ id: 's1', name: 'Kotlin', description: 'Plans', icon: null, professionalUsage: null, displayOrder: 1 }],
        },
      ],
    });
    renderSkills();

    expect(await screen.findByText('Kotlin')).toBeTruthy();
    expect(screen.queryByText(i18n.t('common.loadError'))).toBeNull();
  });
});
