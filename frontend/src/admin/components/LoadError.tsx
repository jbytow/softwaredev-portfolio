import { useTranslation } from 'react-i18next';

interface LoadErrorProps {
  onRetry: () => void;
}

// Shown instead of a form whose data failed to load: saving blank defaults would overwrite stored data
export default function LoadError({ onRetry }: LoadErrorProps) {
  const { t } = useTranslation();

  return (
    <div className="card text-center py-12">
      <p className="text-dark-400 mb-4">{t('common.loadError')}</p>
      <button type="button" onClick={onRetry} className="btn-primary">
        {t('common.retry')}
      </button>
    </div>
  );
}
