const ALLOWED_TAGS = new Set(['P', 'BR', 'STRONG', 'B', 'EM', 'I', 'U', 'A', 'UL', 'OL', 'LI', 'H2', 'H3', 'H4']);

// Elements whose content is code or markup the visitor must never see, so they are dropped
// entirely instead of being unwrapped like harmless containers (div, span).
const DROPPED_WITH_CONTENT = new Set([
  'SCRIPT', 'STYLE', 'IFRAME', 'FRAME', 'OBJECT', 'EMBED', 'APPLET', 'SVG', 'MATH', 'NOSCRIPT',
  'TEMPLATE', 'TEXTAREA', 'SELECT', 'TITLE', 'HEAD', 'META', 'LINK', 'BASE',
]);

const SAFE_HREF = /^(https?:\/\/|mailto:)/i;
const HTTP_HREF = /^https?:\/\//i;
const HAS_TAG = /<\/?[a-z][^>]*>/i;

function escapeHtml(text: string): string {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function plainTextToHtml(text: string): string {
  return text
    .replace(/\r\n?/g, '\n')
    .split(/\n(?:[ \t]*\n)+/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean)
    .map((paragraph) => `<p>${escapeHtml(paragraph).replace(/\n/g, '<br>')}</p>`)
    .join('');
}

function sanitizeChildren(source: Node, target: Node, doc: Document): void {
  source.childNodes.forEach((child) => {
    if (child.nodeType === Node.TEXT_NODE) {
      target.appendChild(doc.createTextNode((child as Text).data));
      return;
    }
    if (child.nodeType !== Node.ELEMENT_NODE) return;

    const element = child as Element;
    const tag = element.tagName.toUpperCase();
    if (DROPPED_WITH_CONTENT.has(tag)) return;
    if (!ALLOWED_TAGS.has(tag)) {
      sanitizeChildren(element, target, doc);
      return;
    }

    // A fresh element carries no attributes, so on*, style and class cannot survive
    const clean = doc.createElement(tag.toLowerCase());
    if (tag === 'A') {
      const href = (element.getAttribute('href') ?? '').trim();
      if (SAFE_HREF.test(href)) {
        clean.setAttribute('href', href);
        if (HTTP_HREF.test(href)) {
          clean.setAttribute('rel', 'noopener noreferrer');
          clean.setAttribute('target', '_blank');
        }
      }
    }
    sanitizeChildren(element, clean, doc);
    target.appendChild(clean);
  });
}

function sanitizeHtml(html: string): string {
  const doc = new DOMParser().parseFromString(html, 'text/html');
  const out = doc.createElement('div');
  sanitizeChildren(doc.body, out, doc);
  return out.innerHTML;
}

/** About text from the admin textarea: plain text keeps its paragraphs, pasted HTML is reduced to an allowlist. */
export function renderAboutText(text: string | null | undefined, placeholder: string): string {
  const source = text ?? '';
  const html = HAS_TAG.test(source) ? sanitizeHtml(source) : plainTextToHtml(source);
  return html.trim() ? html : `<p>${escapeHtml(placeholder)}</p>`;
}
