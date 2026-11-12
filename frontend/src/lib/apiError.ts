import axios from 'axios';

export const FILE_TOO_LARGE_MESSAGE = 'File too large (max 50 MB)';

function fieldErrors(value: unknown): string[] {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return [];
  return Object.entries(value)
    .filter((entry): entry is [string, string] => typeof entry[1] === 'string' && entry[1] !== '')
    .map(([field, message]) => `${field}: ${message}`);
}

// Surfaces the backend's (localized) ApiResponse message instead of a generic failure text.
// 413 is mapped first because a proxy rejects oversized uploads with an HTML body and no message.
export function getApiErrorMessage(err: unknown, fallback: string): string {
  if (!axios.isAxiosError(err) || !err.response) return fallback;
  if (err.response.status === 413) return FILE_TOO_LARGE_MESSAGE;

  const body = err.response.data;
  const message = body?.message;
  if (typeof message !== 'string' || !message) return fallback;

  const details = fieldErrors(body.errors ?? body.data);
  return details.length > 0 ? `${message} (${details.join('; ')})` : message;
}
