import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import i18n from '@/i18n/index';
import AdminPostForm from './AdminPostForm';
import AdminExperienceForm from './AdminExperienceForm';
import AdminSoftSkillForm from './AdminSoftSkillForm';
import AdminSkillCategoryForm from './AdminSkillCategoryForm';
import AdminInterestForm from './AdminInterestForm';
import AdminAchievementForm from './AdminAchievementForm';
import AdminRpgStatForm from './AdminRpgStatForm';
import AdminSettings from './AdminSettings';

const fetches = vi.hoisted(() => ({ count: 0 }));
// Every call of every admin API fails, as when the backend is down
const failing = vi.hoisted(
  () => () =>
    new Proxy(
      {},
      {
        get: () => () => {
          fetches.count += 1;
          return Promise.reject(new Error('backend down'));
        },
      }
    )
);
vi.mock('@/services/api', () => ({
  adminPostsApi: failing(),
  adminExperiencesApi: failing(),
  adminSoftSkillsApi: failing(),
  adminSkillCategoriesApi: failing(),
  adminInterestsApi: failing(),
  adminMediaApi: failing(),
  adminAchievementsApi: failing(),
  adminRpgStatsApi: failing(),
  adminSettingsApi: failing(),
}));
vi.mock('@/admin/components/MediaManager', () => ({ default: () => null }));
vi.mock('@/components/RichTextEditor', () => ({ default: () => null }));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

const screens = [
  { name: 'AdminPostForm', Component: AdminPostForm, path: '/admin/posts/:id/edit', url: '/admin/posts/1/edit' },
  { name: 'AdminExperienceForm', Component: AdminExperienceForm, path: '/admin/experiences/:id/edit', url: '/admin/experiences/1/edit' },
  { name: 'AdminSoftSkillForm', Component: AdminSoftSkillForm, path: '/admin/skills/:id/edit', url: '/admin/skills/1/edit' },
  { name: 'AdminSkillCategoryForm', Component: AdminSkillCategoryForm, path: '/admin/categories/:id/edit', url: '/admin/categories/1/edit' },
  { name: 'AdminInterestForm', Component: AdminInterestForm, path: '/admin/interests/:id/edit', url: '/admin/interests/1/edit' },
  { name: 'AdminAchievementForm', Component: AdminAchievementForm, path: '/admin/achievements/:id/edit', url: '/admin/achievements/1/edit' },
  { name: 'AdminRpgStatForm', Component: AdminRpgStatForm, path: '/admin/rpg-stats/:id/edit', url: '/admin/rpg-stats/1/edit' },
  { name: 'AdminSettings', Component: AdminSettings, path: '/admin/settings', url: '/admin/settings' },
];

describe.each(screens)('$name when loading fails', ({ Component, path, url }) => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    fetches.count = 0;
  });

  it('shows a retry block instead of an empty form that could overwrite stored data', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const { container } = render(
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={[url]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          <Routes>
            <Route path={path} element={<Component />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    await screen.findByText('Could not load this content.');
    expect(container.querySelector('form')).toBeNull();

    const before = fetches.count;
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));
    await waitFor(() => expect(fetches.count).toBeGreaterThan(before));
  });
});
