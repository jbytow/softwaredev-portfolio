import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import i18n from '@/i18n/index';
import AdminInterestForm from './AdminInterestForm';
import AdminRpgStatForm from './AdminRpgStatForm';

vi.mock('@/services/api', () => ({
  adminInterestsApi: { create: vi.fn(), update: vi.fn(), getById: vi.fn() },
  adminRpgStatsApi: { create: vi.fn(), update: vi.fn(), getById: vi.fn() },
  adminMediaApi: { upload: vi.fn() },
}));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

const forms = [
  { name: 'AdminInterestForm', Component: AdminInterestForm, route: '/admin/interests', field: 'titleEn' },
  { name: 'AdminRpgStatForm', Component: AdminRpgStatForm, route: '/admin/rpg-stats', field: 'labelEn' },
];

describe.each(forms)('$name unsaved changes', ({ Component, route, field }) => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  function renderNewForm() {
    const client = new QueryClient();
    return render(
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={[`${route}/new`]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          <Routes>
            <Route path={`${route}/new`} element={<Component />} />
            <Route path={route} element={<div>list page</div>} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  }

  const cancel = () => fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));

  it('leaves a pristine form without asking', () => {
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false);
    renderNewForm();
    cancel();
    expect(confirm).not.toHaveBeenCalled();
    expect(screen.getByText('list page')).toBeTruthy();
  });

  it('asks before cancelling an edited form and stays when declined', () => {
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false);
    const { container } = renderNewForm();
    const input = container.querySelector(`input[name="${field}"]`) as HTMLInputElement;
    fireEvent.change(input, { target: { name: field, value: 'edited' } });

    cancel();

    expect(confirm).toHaveBeenCalledWith(i18n.t('admin.unsaved.confirmLeave'));
    expect(screen.queryByText('list page')).toBeNull();
  });

  it('leaves the edited form when the admin confirms', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const { container } = renderNewForm();
    const input = container.querySelector(`input[name="${field}"]`) as HTMLInputElement;
    fireEvent.change(input, { target: { name: field, value: 'edited' } });

    cancel();

    expect(screen.getByText('list page')).toBeTruthy();
  });
});
