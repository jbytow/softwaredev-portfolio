import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import i18n from '@/i18n/index';
import ConfirmDeleteModal from './ConfirmDeleteModal';

function renderModal(props: Partial<React.ComponentProps<typeof ConfirmDeleteModal>> = {}) {
  const onConfirm = vi.fn();
  const onCancel = vi.fn();
  render(
    <ConfirmDeleteModal
      open
      title="Confirm Delete"
      message="Delete this item?"
      isPending={false}
      onConfirm={onConfirm}
      onCancel={onCancel}
      {...props}
    />
  );
  return { onConfirm, onCancel };
}

describe('ConfirmDeleteModal', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(cleanup);

  it('renders nothing while closed', () => {
    renderModal({ open: false });
    expect(screen.queryByText('Delete this item?')).toBeNull();
  });

  it('shows the title and message', () => {
    renderModal();
    expect(screen.getByText('Confirm Delete')).toBeTruthy();
    expect(screen.getByText('Delete this item?')).toBeTruthy();
  });

  it('confirms and cancels through the buttons', () => {
    const { onConfirm, onCancel } = renderModal();
    fireEvent.click(screen.getByRole('button', { name: i18n.t('common.delete') }));
    fireEvent.click(screen.getByRole('button', { name: i18n.t('common.cancel') }));
    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('disables the confirm button while the request is pending', () => {
    renderModal({ isPending: true });
    const confirm = screen.getByRole('button', { name: i18n.t('common.delete') }) as HTMLButtonElement;
    expect(confirm.disabled).toBe(true);
  });
});
