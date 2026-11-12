import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios';
import { describe, expect, it } from 'vitest';
import { FILE_TOO_LARGE_MESSAGE, getApiErrorMessage } from './apiError';

function axiosError(status: number | undefined, data?: unknown): AxiosError {
  const config = { headers: new AxiosHeaders() };
  const response: AxiosResponse | undefined =
    status === undefined
      ? undefined
      : { status, statusText: '', headers: {}, config, data };
  return new AxiosError('Request failed', undefined, config, {}, response);
}

describe('getApiErrorMessage', () => {
  it('returns the server message', () => {
    const err = axiosError(409, { success: false, message: 'Slug already exists' });
    expect(getApiErrorMessage(err, 'Failed to save')).toBe('Slug already exists');
  });

  it('appends field errors from data.errors', () => {
    const err = axiosError(400, {
      success: false,
      message: 'Validation failed',
      errors: { titleEn: 'must not be blank', slug: 'too long' },
    });
    expect(getApiErrorMessage(err, 'Failed to save')).toBe(
      'Validation failed (titleEn: must not be blank; slug: too long)'
    );
  });

  it('appends field errors from data.data', () => {
    const err = axiosError(400, {
      success: false,
      message: 'Validation failed',
      data: { titleEn: 'must not be blank' },
    });
    expect(getApiErrorMessage(err, 'Failed to save')).toBe(
      'Validation failed (titleEn: must not be blank)'
    );
  });

  it('ignores non-string field error values and an empty map', () => {
    const err = axiosError(400, {
      message: 'Validation failed',
      errors: { a: 1, b: null },
    });
    expect(getApiErrorMessage(err, 'Failed to save')).toBe('Validation failed');
  });

  it('maps 413 to the size message even without a body', () => {
    expect(getApiErrorMessage(axiosError(413, '<html>413</html>'), 'Failed to upload')).toBe(
      FILE_TOO_LARGE_MESSAGE
    );
    expect(getApiErrorMessage(axiosError(413, { message: 'File size exceeds' }), 'x')).toBe(
      FILE_TOO_LARGE_MESSAGE
    );
  });

  it('falls back when the response has no usable message', () => {
    expect(getApiErrorMessage(axiosError(500, '<html>Bad gateway</html>'), 'Failed to save')).toBe(
      'Failed to save'
    );
    expect(getApiErrorMessage(axiosError(500, { message: '' }), 'Failed to save')).toBe(
      'Failed to save'
    );
    expect(getApiErrorMessage(axiosError(500, { message: 42 }), 'Failed to save')).toBe(
      'Failed to save'
    );
  });

  it('falls back on a network error without a response', () => {
    expect(getApiErrorMessage(axiosError(undefined), 'Failed to save')).toBe('Failed to save');
  });

  it.each([new Error('boom'), 'text', null, undefined, { response: { data: { message: 'x' } } }])(
    'falls back for a non-axios error %#',
    (err) => {
      expect(getApiErrorMessage(err, 'Failed to save')).toBe('Failed to save');
    }
  );
});
