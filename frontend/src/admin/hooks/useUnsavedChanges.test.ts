import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { cleanup, renderHook } from '@testing-library/react';
import i18n from '@/i18n/index';
import { hasUnsavedChanges, useUnsavedChanges } from './useUnsavedChanges';

const beforeUnloadCalls = (spy: { mock: { calls: unknown[][] } }) =>
  spy.mock.calls.filter(([type]) => type === 'beforeunload');

describe('useUnsavedChanges', () => {
  beforeAll(async () => {
    await i18n.changeLanguage('en');
  });
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('registers a beforeunload handler only while dirty', () => {
    const add = vi.spyOn(window, 'addEventListener');
    const { rerender } = renderHook(({ dirty }) => useUnsavedChanges(dirty), {
      initialProps: { dirty: false },
    });
    expect(beforeUnloadCalls(add)).toHaveLength(0);

    rerender({ dirty: true });
    expect(beforeUnloadCalls(add)).toHaveLength(1);
  });

  it('removes the handler when the form turns clean again', () => {
    const add = vi.spyOn(window, 'addEventListener');
    const remove = vi.spyOn(window, 'removeEventListener');
    const { rerender } = renderHook(({ dirty }) => useUnsavedChanges(dirty), {
      initialProps: { dirty: true },
    });
    const handler = beforeUnloadCalls(add)[0][1];

    rerender({ dirty: false });
    expect(remove).toHaveBeenCalledWith('beforeunload', handler);
  });

  it('removes the handler on unmount', () => {
    const add = vi.spyOn(window, 'addEventListener');
    const remove = vi.spyOn(window, 'removeEventListener');
    const { unmount } = renderHook(() => useUnsavedChanges(true));
    const handler = beforeUnloadCalls(add)[0][1];

    unmount();
    expect(remove).toHaveBeenCalledWith('beforeunload', handler);
  });

  it('asks the browser to show its leave prompt', () => {
    const add = vi.spyOn(window, 'addEventListener');
    renderHook(() => useUnsavedChanges(true));
    const handler = beforeUnloadCalls(add)[0][1] as (event: unknown) => void;
    // A plain object: jsdom's Event.returnValue is the legacy boolean, not the string Chrome checks
    const event = { preventDefault: vi.fn(), returnValue: undefined as unknown };

    handler(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(event.returnValue).toBe('');
  });

  it('leaves the event alone when clean', () => {
    renderHook(() => useUnsavedChanges(false));
    const event = new Event('beforeunload', { cancelable: true }) as BeforeUnloadEvent;
    window.dispatchEvent(event);
    expect(event.defaultPrevented).toBe(false);
  });

  it('confirmLeave does not prompt when clean', () => {
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false);
    const { result } = renderHook(() => useUnsavedChanges(false));
    expect(result.current.confirmLeave()).toBe(true);
    expect(confirm).not.toHaveBeenCalled();
  });

  it.each([
    [true, true],
    [false, false],
  ])('confirmLeave returns the answer %s when dirty', (answer, expected) => {
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(answer);
    const { result } = renderHook(() => useUnsavedChanges(true));
    expect(result.current.confirmLeave()).toBe(expected);
    expect(confirm).toHaveBeenCalledWith(i18n.t('admin.unsaved.confirmLeave'));
  });

  it('publishes the dirty state for the layout and clears it when clean or unmounted', () => {
    const { rerender, unmount } = renderHook(({ dirty }) => useUnsavedChanges(dirty), {
      initialProps: { dirty: false },
    });
    expect(hasUnsavedChanges()).toBe(false);
    rerender({ dirty: true });
    expect(hasUnsavedChanges()).toBe(true);
    rerender({ dirty: false });
    expect(hasUnsavedChanges()).toBe(false);
    rerender({ dirty: true });
    unmount();
    expect(hasUnsavedChanges()).toBe(false);
  });
});
