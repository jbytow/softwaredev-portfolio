import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import i18n from '@/i18n/index';
import ErrorBoundary from './ErrorBoundary';

function Broken(): never {
  throw new Error('render failed');
}

describe('ErrorBoundary', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  beforeEach(() => {
    // React logs every caught render error; the boundary under test makes that noise expected
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('renders its children while nothing throws', () => {
    render(
      <ErrorBoundary>
        <p>fine</p>
      </ErrorBoundary>
    );
    expect(screen.getByText('fine')).toBeTruthy();
  });

  it('shows the translated error screen in the active language', async () => {
    render(
      <ErrorBoundary>
        <Broken />
      </ErrorBoundary>
    );
    expect(screen.getByRole('heading', { name: 'Something went wrong' })).toBeTruthy();

    cleanup();
    await i18n.changeLanguage('pl');
    const polishTitle = i18n.t('errors.boundary.title');
    expect(polishTitle).not.toBe('Something went wrong');
    render(
      <ErrorBoundary>
        <Broken />
      </ErrorBoundary>
    );
    expect(screen.getByRole('heading', { name: polishTitle })).toBeTruthy();
    await i18n.changeLanguage('en');
  });

  it('reloads the page from the button, because a rejected lazy import stays cached', () => {
    const reload = vi.fn();
    vi.stubGlobal('location', { ...window.location, reload });
    render(
      <ErrorBoundary>
        <Broken />
      </ErrorBoundary>
    );
    fireEvent.click(screen.getByRole('button', { name: i18n.t('errors.boundary.reload') }));
    expect(reload).toHaveBeenCalledTimes(1);
  });
});
