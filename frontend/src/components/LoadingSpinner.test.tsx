import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render } from '@testing-library/react';
import LoadingSpinner from './LoadingSpinner';

describe('LoadingSpinner', () => {
  afterEach(cleanup);

  it('renders the coffee cup inline with its three steam wisps', () => {
    const { container } = render(<LoadingSpinner />);
    expect(container.querySelector('.coffee-cup')).not.toBeNull();
    expect(container.querySelectorAll('.coffee-steam')).toHaveLength(3);
    expect(container.firstElementChild?.className).not.toContain('fixed');
  });

  it('covers the viewport in full-screen mode', () => {
    const { container } = render(<LoadingSpinner fullScreen />);
    expect(container.firstElementChild?.className).toContain('fixed');
    expect(container.querySelector('.coffee-cup')).not.toBeNull();
  });
});
