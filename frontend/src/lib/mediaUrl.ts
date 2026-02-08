const API_BASE = import.meta.env.VITE_API_URL || '';

export function getMediaUrl(url: string | null | undefined): string {
  if (!url) return '';

  // YouTube and other absolute URLs - return as-is
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }

  // For relative API URLs, prepend the API base if set, otherwise use as-is
  // In development without VITE_API_URL, the Vite proxy should handle /api/* paths
  // In production, VITE_API_URL should be set to the backend URL if different from frontend
  if (API_BASE && url.startsWith('/api/')) {
    return API_BASE.replace(/\/api$/, '') + url;
  }

  return url;
}
