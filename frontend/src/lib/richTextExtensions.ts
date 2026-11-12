import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import TextAlign from '@tiptap/extension-text-align';

// One schema for the admin editor and the public renderer: a node the editor can create
// but the renderer does not know makes generateHTML throw for the whole post.
export const richTextExtensions = [
  StarterKit.configure({
    heading: {
      levels: [1, 2, 3],
    },
  }),
  Underline,
  Link.configure({
    openOnClick: false,
    HTMLAttributes: {
      class: 'text-primary-400 underline hover:text-primary-300',
      target: '_blank',
      rel: 'noopener noreferrer',
    },
  }),
  TextAlign.configure({
    types: ['heading', 'paragraph'],
  }),
];

// A bare host like "example.com" would be stored as a relative URL and resolve under the
// current page (/projects/example.com), so links without a scheme default to https.
export function normalizeLinkHref(href: string): string {
  const trimmed = href.trim();
  // "host:port" looks like "scheme:" but is not one, so a scheme needs "//" unless it is mailto/tel/sms
  if (/^[a-z][a-z0-9+.-]*:\/\//i.test(trimmed) || /^(mailto|tel|sms):/i.test(trimmed) || /^[/#]/.test(trimmed)) {
    return trimmed;
  }
  return `https://${trimmed}`;
}
