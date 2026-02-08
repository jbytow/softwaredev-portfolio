import { generateHTML } from '@tiptap/html';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import TextAlign from '@tiptap/extension-text-align';
import { useMemo } from 'react';

interface RichTextContentProps {
  content: Record<string, unknown>;
  className?: string;
}

export default function RichTextContent({ content, className = '' }: RichTextContentProps) {
  const html = useMemo(() => {
    if (!content || Object.keys(content).length === 0) {
      return '';
    }

    try {
      return generateHTML(content, [
        StarterKit.configure({
          heading: {
            levels: [1, 2, 3],
          },
        }),
        Underline,
        Link.configure({
          openOnClick: true,
          HTMLAttributes: {
            class: 'text-primary-400 underline hover:text-primary-300',
            target: '_blank',
            rel: 'noopener noreferrer',
          },
        }),
        TextAlign.configure({
          types: ['heading', 'paragraph'],
        }),
      ]);
    } catch {
      // Failed to render content
      return '';
    }
  }, [content]);

  if (!html) {
    return null;
  }

  return (
    <div
      className={`prose prose-invert prose-lg max-w-none ${className}`}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
}
