import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter, useNavigate } from 'react-router-dom';
import ScrollToTop from './ScrollToTop';

let navigate: ReturnType<typeof useNavigate>;
function Probe() {
  navigate = useNavigate();
  return <p>page</p>;
}

function renderScrollToTop(initialEntry = '/list') {
  return render(
    <MemoryRouter initialEntries={['/start', initialEntry]} initialIndex={1}>
      <ScrollToTop />
      <Probe />
    </MemoryRouter>
  );
}

describe('ScrollToTop', () => {
  const scrollTo = vi.fn();

  beforeEach(() => {
    window.scrollTo = scrollTo;
  });
  afterEach(() => {
    cleanup();
    scrollTo.mockReset();
  });

  it('does not scroll on the first render', () => {
    renderScrollToTop();
    expect(scrollTo).not.toHaveBeenCalled();
  });

  it('scrolls to the top once when navigating to another path', () => {
    renderScrollToTop();
    act(() => navigate('/detail'));

    expect(scrollTo).toHaveBeenCalledTimes(1);
    expect(scrollTo).toHaveBeenCalledWith({ top: 0, left: 0, behavior: 'instant' });
  });

  it('scrolls to the top when a path is replaced', () => {
    renderScrollToTop();
    act(() => navigate('/detail', { replace: true }));
    expect(scrollTo).toHaveBeenCalledTimes(1);
  });

  it('leaves the position alone on back and forward', () => {
    renderScrollToTop();
    act(() => navigate('/detail'));
    scrollTo.mockClear();

    act(() => navigate(-1));
    expect(screen.getByText('page')).toBeTruthy();
    expect(scrollTo).not.toHaveBeenCalled();

    act(() => navigate(1));
    expect(scrollTo).not.toHaveBeenCalled();
  });

  it('keeps the position when only the search params change', () => {
    renderScrollToTop();
    act(() => navigate('/list?hashtag=seo'));
    act(() => navigate('/list?hashtag=ads'));
    act(() => navigate('/list', { replace: true }));

    expect(scrollTo).not.toHaveBeenCalled();
  });
});
