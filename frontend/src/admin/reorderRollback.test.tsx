import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen, waitFor, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import i18n from '@/i18n/index';
import AdminInterests from './AdminInterests';
import AdminAchievements from './AdminAchievements';
import AdminRpgStats from './AdminRpgStats';

const api = vi.hoisted(() => ({
  interests: { getAll: vi.fn(), reorder: vi.fn(), delete: vi.fn() },
  achievements: { getAll: vi.fn(), reorder: vi.fn(), delete: vi.fn() },
  rpgStats: { getAll: vi.fn(), reorder: vi.fn(), delete: vi.fn() },
}));
vi.mock('@/services/api', () => ({
  adminInterestsApi: api.interests,
  adminAchievementsApi: api.achievements,
  adminRpgStatsApi: api.rpgStats,
}));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

// jsdom has no layout, so the drag is simulated by calling the handler the list hands to DndContext
const dnd = vi.hoisted(() => ({ onDragEnd: null as null | ((event: unknown) => void) }));
vi.mock('@dnd-kit/core', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@dnd-kit/core')>();
  return {
    ...actual,
    DndContext: ({ children, onDragEnd }: { children: React.ReactNode; onDragEnd: (event: unknown) => void }) => {
      dnd.onDragEnd = onDragEnd;
      return <>{children}</>;
    },
  };
});

const rows = (titleField: string) =>
  ['a', 'b'].map((id, index) => ({ id, [titleField]: `Item ${id}`, displayOrder: index }));

const cases = [
  { name: 'AdminInterests', Component: AdminInterests, api: api.interests, titleField: 'titleEn' },
  { name: 'AdminAchievements', Component: AdminAchievements, api: api.achievements, titleField: 'titleEn' },
  { name: 'AdminRpgStats', Component: AdminRpgStats, api: api.rpgStats, titleField: 'labelEn' },
];

describe.each(cases)('$name reorder', ({ Component, api: listApi, titleField }) => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('puts the list back in the server order when saving the new order fails', async () => {
    listApi.getAll.mockResolvedValue({ data: rows(titleField) });
    listApi.reorder.mockRejectedValue(new Error('boom'));
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          <Component />
        </MemoryRouter>
      </QueryClientProvider>
    );
    await screen.findByText('Item a');
    const order = () => screen.getAllByText(/^Item /).map((el) => el.textContent);
    expect(order()).toEqual(['Item a', 'Item b']);

    act(() => dnd.onDragEnd?.({ active: { id: 'a' }, over: { id: 'b' } }));
    await waitFor(() => expect(order()).toEqual(['Item b', 'Item a']));

    await waitFor(() => expect(listApi.reorder).toHaveBeenCalled());
    await waitFor(() => expect(order()).toEqual(['Item a', 'Item b']));
  });
});
