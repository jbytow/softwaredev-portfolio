import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useSiteSettings } from './useSiteSettings';
import { queryKeys } from './queryKeys';

const get = vi.hoisted(() => vi.fn());
const language = vi.hoisted(() => ({ current: 'en' }));
vi.mock('@/services/api', () => ({ settingsApi: { get } }));
vi.mock('@/contexts/LanguageContext', () => ({
  useLanguage: () => ({ language: language.current }),
}));

function Reader({ label }: { label: string }) {
  const { data } = useSiteSettings();
  return <p>{`${label}:${data?.data?.heroTitle ?? 'none'}`}</p>;
}

function setup(client: QueryClient, labels: string[]) {
  return render(
    <QueryClientProvider client={client}>
      {labels.map((label) => (
        <Reader key={label} label={label} />
      ))}
    </QueryClientProvider>
  );
}

describe('useSiteSettings', () => {
  afterEach(() => {
    cleanup();
    get.mockReset();
    language.current = 'en';
  });

  it('shares one request between all components that read the settings', async () => {
    get.mockResolvedValue({ success: true, data: { heroTitle: 'Portfolio' } });
    setup(new QueryClient(), ['footer', 'home', 'contact']);

    await waitFor(() => expect(screen.getAllByText(/:Portfolio$/)).toHaveLength(3));
    expect(get).toHaveBeenCalledTimes(1);
  });

  it('caches the settings under the public per-language key', async () => {
    get.mockResolvedValue({ success: true, data: { heroTitle: 'Portfolio' } });
    language.current = 'pl';
    const client = new QueryClient();
    setup(client, ['footer']);

    await waitFor(() => expect(screen.getByText('footer:Portfolio')).toBeTruthy());
    expect(client.getQueryData(queryKeys.settings('pl'))).toEqual({ success: true, data: { heroTitle: 'Portfolio' } });
    expect(client.getQueryData(queryKeys.settings('en'))).toBeUndefined();
  });
});
