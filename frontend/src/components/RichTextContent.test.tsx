import { afterEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { generateHTML, useEditor, type AnyExtension, type Editor } from '@tiptap/react';
import RichTextContent from './RichTextContent';
import RichTextEditor from './RichTextEditor';
import { richTextExtensions } from '@/lib/richTextExtensions';

// Pass-through spies: real rendering, but the extension lists each component uses stay observable
vi.mock('@tiptap/react', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@tiptap/react')>();
  return { ...actual, generateHTML: vi.fn(actual.generateHTML), useEditor: vi.fn(actual.useEditor) };
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

const paragraph = (text: string) => ({ type: 'paragraph', content: [{ type: 'text', text }] });

const linkParagraph = (href: string) => ({
  type: 'paragraph',
  content: [{ type: 'text', text: 'click', marks: [{ type: 'link', attrs: { href } }] }],
});

// Every node, mark and alignment the editor can produce
const fullDocument = {
  type: 'doc',
  content: [
    { type: 'heading', attrs: { level: 1, textAlign: 'left' }, content: [{ type: 'text', text: 'Title' }] },
    { type: 'heading', attrs: { level: 2, textAlign: 'center' }, content: [{ type: 'text', text: 'Section' }] },
    { type: 'heading', attrs: { level: 3, textAlign: 'right' }, content: [{ type: 'text', text: 'Sub' }] },
    {
      type: 'paragraph',
      attrs: { textAlign: 'justify' },
      content: [
        { type: 'text', text: 'bold', marks: [{ type: 'bold' }] },
        { type: 'text', text: ' italic', marks: [{ type: 'italic' }] },
        { type: 'text', text: ' underline', marks: [{ type: 'underline' }] },
        { type: 'text', text: ' strike', marks: [{ type: 'strike' }] },
        { type: 'text', text: ' code', marks: [{ type: 'code' }] },
        { type: 'hardBreak' },
        {
          type: 'text',
          text: 'link',
          marks: [{ type: 'link', attrs: { href: 'https://example.com', target: '_blank' } }],
        },
        { type: 'text', text: ' <script>alert(1)</script> & "quotes"' },
      ],
    },
    { type: 'blockquote', content: [paragraph('Quote')] },
    { type: 'bulletList', content: [{ type: 'listItem', content: [paragraph('Bullet')] }] },
    { type: 'orderedList', attrs: { start: 1 }, content: [{ type: 'listItem', content: [paragraph('Numbered')] }] },
    { type: 'codeBlock', attrs: { language: null }, content: [{ type: 'text', text: 'const x = 1;' }] },
    { type: 'horizontalRule' },
  ],
};

// Serialises a fragment with sorted attributes and collapsed whitespace, so the comparison
// ignores formatting differences and catches only real markup differences
function normaliseHtml(html: string): string {
  const template = document.createElement('template');
  template.innerHTML = html;
  const serialise = (node: Node): string => {
    if (node.nodeType === Node.TEXT_NODE) {
      return (node.textContent ?? '').replace(/\s+/g, ' ');
    }
    if (!(node instanceof Element)) {
      return '';
    }
    const attributes = Array.from(node.attributes)
      .map((attribute) => `${attribute.name}="${attribute.value}"`)
      .sort()
      .join(' ');
    const children = Array.from(node.childNodes).map(serialise).join('');
    const tag = node.tagName.toLowerCase();
    return `<${tag}${attributes ? ` ${attributes}` : ''}>${children}</${tag}>`;
  };
  return Array.from(template.content.childNodes).map(serialise).join('');
}

function rendererExtensions(): AnyExtension[] {
  render(<RichTextContent content={{ type: 'doc', content: [paragraph('x')] }} />);
  return vi.mocked(generateHTML).mock.lastCall![1];
}

function editorExtensions(): AnyExtension[] {
  render(<RichTextEditor content={{}} onChange={() => {}} />);
  return vi.mocked(useEditor).mock.lastCall![0]!.extensions!;
}

describe('RichTextContent', () => {
  it('renders the text of a document with an unknown node type instead of a blank body', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    const doc = {
      type: 'doc',
      content: [
        paragraph('First paragraph'),
        { type: 'image', attrs: { src: '/x.png' } },
        { type: 'callout', content: [paragraph('Inside unknown block')] },
        { type: 'bulletList', content: [{ type: 'listItem', content: [paragraph('List item')] }] },
      ],
    };

    const { container } = render(<RichTextContent content={doc} />);

    expect(container.textContent).toContain('First paragraph');
    expect(container.textContent).toContain('Inside unknown block');
    expect(container.textContent).toContain('List item');
    expect(container.querySelectorAll('p')).toHaveLength(3);
    expect(consoleError).toHaveBeenCalled();
  });

  it('renders the text of a document with an unknown mark instead of a blank body', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    const doc = {
      type: 'doc',
      content: [
        {
          type: 'paragraph',
          content: [
            { type: 'text', text: 'Plain ' },
            { type: 'text', text: 'highlighted', marks: [{ type: 'highlight' }] },
          ],
        },
      ],
    };

    const { container } = render(<RichTextContent content={doc} />);

    expect(container.textContent).toBe('Plain highlighted');
    expect(consoleError).toHaveBeenCalled();
  });

  it('renders every node, mark and alignment exactly like generateHTML with the shared extensions', () => {
    const { container } = render(<RichTextContent content={fullDocument} />);

    const rendered = container.firstElementChild!.innerHTML;
    expect(normaliseHtml(rendered)).toBe(normaliseHtml(generateHTML(fullDocument, richTextExtensions)));
    expect(rendered).toContain('<u> underline</u>');
    expect(container.querySelector('script')).toBeNull();
    const link = container.querySelector('a')!;
    expect(link.getAttribute('href')).toBe('https://example.com');
    expect(link.getAttribute('rel')).toBe('noopener noreferrer');
  });

  it('keeps the text alignment set in the editor', () => {
    const { container } = render(<RichTextContent content={fullDocument} />);

    const alignments = Array.from(container.querySelectorAll('h1, h2, h3, p[style]')).map(
      (element) => (element as HTMLElement).style.textAlign
    );
    expect(alignments).toEqual(['left', 'center', 'right', 'justify']);
  });

  it.each([
    'javascript:alert(1)',
    'JAVASCRIPT:alert(1)',
    ' javascript:alert(1)',
    'java\tscript:alert(1)',
    'vbscript:msgbox(1)',
    'data:text/html,<script>alert(1)</script>',
  ])('renders a link to %j without an href', (href) => {
    const { container } = render(<RichTextContent content={{ type: 'doc', content: [linkParagraph(href)] }} />);

    const link = container.querySelector('a')!;
    expect(link.textContent).toBe('click');
    expect(link.getAttribute('href') ?? '').toBe('');
  });

  it.each(['https://example.com', 'mailto:a@b.pl', '/about', '#top'])('keeps a safe link to %j', (href) => {
    const { container } = render(<RichTextContent content={{ type: 'doc', content: [linkParagraph(href)] }} />);

    expect(container.querySelector('a')!.getAttribute('href')).toBe(href);
  });

  it('renders nothing for an empty document', () => {
    const { container } = render(<RichTextContent content={{}} />);

    expect(container.innerHTML).toBe('');
  });
});

function renderEditor(content: Record<string, unknown>): Editor {
  render(<RichTextEditor content={content} onChange={() => {}} />);
  const results = vi.mocked(useEditor).mock.results;
  return results[results.length - 1].value as Editor;
}

describe('RichTextEditor toolbar', () => {
  // fireEvent.click is what Enter or Space on a focused button dispatches
  it.each([
    ['Bold (Ctrl+B)', (editor: Editor) => editor.isActive('bold')],
    ['Heading 2', (editor: Editor) => editor.isActive('heading', { level: 2 })],
    ['Align Center', (editor: Editor) => editor.isActive({ textAlign: 'center' })],
    ['Bullet List', (editor: Editor) => editor.isActive('bulletList')],
  ])('runs %j on click and reports it as pressed', (title, isApplied) => {
    const editor = renderEditor({ type: 'doc', content: [paragraph('Hello')] });
    act(() => {
      editor.commands.selectAll();
    });

    fireEvent.click(screen.getByTitle(title));

    expect(isApplied(editor)).toBe(true);
    expect(screen.getByTitle(title).getAttribute('aria-pressed')).toBe('true');
    expect(screen.getByTitle('Strikethrough').getAttribute('aria-pressed')).toBe('false');
  });

  it.each([
    ['brand.pl/kampania', 'https://brand.pl/kampania'],
    ['  example.com  ', 'https://example.com'],
    ['example.com:8080/x', 'https://example.com:8080/x'],
    ['localhost:5173', 'https://localhost:5173'],
    ['/about', '/about'],
    ['mailto:a@b.pl', 'mailto:a@b.pl'],
  ])('stores the link %j as %j', (entered, stored) => {
    vi.spyOn(window, 'prompt').mockReturnValue(entered);
    const editor = renderEditor({ type: 'doc', content: [paragraph('Hello')] });
    act(() => {
      editor.commands.selectAll();
    });

    fireEvent.click(screen.getByTitle('Add Link'));

    expect(editor.getAttributes('link').href).toBe(stored);
  });

  it('keeps the focused toolbar button mounted while the text changes', () => {
    const editor = renderEditor({ type: 'doc', content: [paragraph('Hello')] });
    const bold = screen.getByTitle('Bold (Ctrl+B)');
    bold.focus();

    act(() => {
      editor.commands.insertContent(' world');
    });

    expect(editor.getText()).toBe('Hello world');
    expect(screen.getByTitle('Bold (Ctrl+B)')).toBe(bold);
    expect(document.activeElement).toBe(bold);
  });
});

describe('RichTextEditor and RichTextContent', () => {
  it('use the same extensions with the same options (editor adds only the placeholder)', () => {
    const renderer = rendererExtensions();
    const editor = editorExtensions().filter((extension) => extension.name !== 'placeholder');

    expect(editor.map((extension) => extension.name)).toEqual(renderer.map((extension) => extension.name));
    editor.forEach((extension, index) => {
      expect(extension.options, extension.name).toEqual(renderer[index].options);
    });
  });
});
