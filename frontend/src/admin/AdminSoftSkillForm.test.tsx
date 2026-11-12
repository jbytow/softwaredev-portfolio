import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import i18n from '@/i18n/index';
import AdminSoftSkillForm from './AdminSoftSkillForm';

const api = vi.hoisted(() => ({
  skills: { getById: vi.fn(), create: vi.fn(), update: vi.fn() },
  categories: { getAll: vi.fn() },
}));
vi.mock('@/services/api', () => ({
  adminSoftSkillsApi: api.skills,
  adminSkillCategoriesApi: api.categories,
}));
vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

const skill = {
  id: 's1',
  nameEn: 'Empathy',
  namePl: 'Empatia',
  categoryId: 'c1',
  displayOrder: 0,
};

function renderEdit() {
  api.categories.getAll.mockResolvedValue({
    success: true,
    data: [{ id: 'c1', nameEn: 'People', namePl: 'Ludzie' }],
  });
  api.skills.getById.mockResolvedValue({ success: true, data: skill });
  api.skills.update.mockResolvedValue({ success: true });
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }} initialEntries={['/admin/soft-skills/s1/edit']}>
        <Routes>
          <Route path="/admin/soft-skills/:id/edit" element={<AdminSoftSkillForm />} />
          <Route path="/admin/soft-skills" element={<div>list</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

describe('AdminSoftSkillForm category', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('sends categoryId null when the category is cleared, so the backend removes it', async () => {
    renderEdit();
    const select = (await screen.findByDisplayValue('People')) as HTMLSelectElement;
    fireEvent.change(select, { target: { value: '' } });
    fireEvent.submit(document.querySelector('form') as HTMLFormElement);
    await waitFor(() => expect(api.skills.update).toHaveBeenCalledTimes(1));
    expect(api.skills.update.mock.calls[0][1].categoryId).toBeNull();
  });

  it('keeps the chosen category id', async () => {
    renderEdit();
    await screen.findByDisplayValue('People');
    fireEvent.submit(document.querySelector('form') as HTMLFormElement);
    await waitFor(() => expect(api.skills.update).toHaveBeenCalledTimes(1));
    expect(api.skills.update.mock.calls[0][1].categoryId).toBe('c1');
  });
});
