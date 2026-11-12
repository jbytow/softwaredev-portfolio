import { useTranslation } from 'react-i18next';

interface ConfirmDeleteModalProps {
  open: boolean;
  title: string;
  message: string;
  isPending: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmDeleteModal({
  open,
  title,
  message,
  isPending,
  onConfirm,
  onCancel,
}: ConfirmDeleteModalProps) {
  const { t } = useTranslation();

  if (!open) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="card max-w-md w-full mx-4">
        <h3 className="text-lg font-semibold text-dark-100 mb-2">{title}</h3>
        <p className="text-dark-400 mb-6">{message}</p>
        <div className="flex justify-end space-x-3">
          <button onClick={onCancel} className="btn-secondary">
            {t('common.cancel')}
          </button>
          <button
            onClick={onConfirm}
            disabled={isPending}
            className="btn-primary bg-red-500 hover:bg-red-600 disabled:opacity-50"
          >
            {t('common.delete')}
          </button>
        </div>
      </div>
    </div>
  );
}
