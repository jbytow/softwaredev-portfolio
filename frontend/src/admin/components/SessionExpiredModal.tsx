import { useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';

interface SessionExpiredModalProps {
  open: boolean;
  onClose: () => void;
}

export default function SessionExpiredModal({ open, onClose }: SessionExpiredModalProps) {
  const { t } = useTranslation();
  const loginButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) return;
    loginButtonRef.current?.focus();
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  // noopener: the login tab must not get a handle on this tab, which holds the unsaved form
  const openLogin = () => window.open('/admin/login', '_blank', 'noopener');

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="session-expired-title"
        aria-describedby="session-expired-body"
        className="card max-w-md w-full mx-4"
      >
        <h3 id="session-expired-title" className="text-lg font-semibold text-dark-100 mb-2">
          {t('admin.session.expiredTitle')}
        </h3>
        <p id="session-expired-body" className="text-dark-400 mb-6">
          {t('admin.session.expiredBody')}
        </p>
        <div className="flex justify-end space-x-3">
          <button type="button" onClick={onClose} className="btn-secondary">
            {t('admin.session.close')}
          </button>
          <button type="button" ref={loginButtonRef} onClick={openLogin} className="btn-primary">
            {t('admin.session.openLogin')}
          </button>
        </div>
      </div>
    </div>
  );
}
