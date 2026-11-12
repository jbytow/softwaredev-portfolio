// The core generateHTML serialises with the browser DOM; @tiptap/html's zeed-dom serializer
// silently drops style attributes, which removed every text alignment from public posts.
import { generateHTML } from '@tiptap/react';
import { useMemo } from 'react';
import { richTextExtensions } from '@/lib/richTextExtensions';

interface RichTextContentProps {
  content: Record<string, unknown>;
  className?: string;
}

type RenderedContent = { html: string } | { paragraphs: string[] };

function childrenOf(node: unknown): unknown[] {
  const content = (node as { content?: unknown } | null)?.content;
  return Array.isArray(content) ? content : [];
}

function textOf(node: unknown): string | undefined {
  const text = (node as { text?: unknown } | null)?.text;
  return typeof text === 'string' ? text : undefined;
}

function inlineText(node: unknown): string {
  return textOf(node) ?? childrenOf(node).map(inlineText).join('');
}

// Every node that directly holds text becomes one paragraph, so a document the schema
// rejects still shows its words in reading order.
function textParagraphs(node: unknown): string[] {
  const children = childrenOf(node);
  if (children.some((child) => textOf(child) !== undefined)) {
    return [children.map(inlineText).join('')];
  }
  return children.flatMap(textParagraphs);
}

function renderContent(content: Record<string, unknown>): RenderedContent {
  try {
    return { html: generateHTML(content, richTextExtensions) };
  } catch (error) {
    // Schema drift must stay visible to the owner without hiding the post body from visitors
    console.error('RichTextContent: falling back to plain text', error);
    return { paragraphs: textParagraphs(content).filter((text) => text.trim() !== '') };
  }
}

export default function RichTextContent({ content, className = '' }: RichTextContentProps) {
  const rendered = useMemo(
    () => (!content || Object.keys(content).length === 0 ? null : renderContent(content)),
    [content]
  );

  const classes = `prose prose-invert prose-lg max-w-none ${className}`;

  if (!rendered) {
    return null;
  }

  if ('paragraphs' in rendered) {
    if (rendered.paragraphs.length === 0) {
      return null;
    }
    return (
      <div className={classes}>
        {rendered.paragraphs.map((text, index) => (
          <p key={index}>{text}</p>
        ))}
      </div>
    );
  }

  if (!rendered.html) {
    return null;
  }

  // generateHTML serialises through the ProseMirror schema (text is escaped, Link strips unsafe hrefs)
  return <div className={classes} dangerouslySetInnerHTML={{ __html: rendered.html }} />;
}
