import { useQuery } from '@tanstack/react-query';
import { settingsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';

export function useSiteSettings() {
  const { language } = useLanguage();
  return useQuery({
    queryKey: queryKeys.settings(language),
    queryFn: () => settingsApi.get(),
  });
}
