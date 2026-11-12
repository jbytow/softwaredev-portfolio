import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, render } from '@testing-library/react';
import LoadingSpinner from './LoadingSpinner';

describe('LoadingSpinner', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });
  afterEach(() => {
    cleanup();
    vi.useRealTimers();
  });

  it('renders the coffee cup inline with its three steam wisps straight away', () => {
    const { container } = render(<LoadingSpinner />);
    expect(container.querySelector('.coffee-cup')).not.toBeNull();
    expect(container.querySelectorAll('.coffee-steam')).toHaveLength(3);
    expect(container.firstElementChild?.className).not.toContain('fixed');
  });

  it('keeps the full-screen overlay hidden for the first 200 ms so fast loads do not flash it', () => {
    const { container } = render(<LoadingSpinner fullScreen />);
    expect(container.firstElementChild).toBeNull();

    act(() => vi.advanceTimersByTime(199));
    expect(container.firstElementChild).toBeNull();
  });

  it('covers the viewport with the coffee cup once 200 ms have passed', () => {
    const { container } = render(<LoadingSpinner fullScreen />);
    act(() => vi.advanceTimersByTime(200));
    expect(container.firstElementChild?.className).toContain('fixed');
    expect(container.querySelector('.coffee-cup')).not.toBeNull();
  });

  it('never shows the overlay when it unmounts before the delay ends', () => {
    const { container, unmount } = render(<LoadingSpinner fullScreen />);
    unmount();
    act(() => vi.advanceTimersByTime(500));
    expect(container.firstElementChild).toBeNull();
  });
});
