import { useTranslation } from 'react-i18next';
import { useNerdMode } from '@/contexts/NerdModeContext';

export function useNerdTranslation() {
  const { t, i18n } = useTranslation();
  const { nerdMode } = useNerdMode();

  const tn = (key: string, options?: Record<string, unknown>): string => {
    if (nerdMode) {
      const nerdKey = key + '_nerd';
      if (i18n.exists(nerdKey)) {
        return String(t(nerdKey, options as never));
      }
    }
    return String(t(key, options as never));
  };

  return { t: tn, nerdMode, i18n };
}
