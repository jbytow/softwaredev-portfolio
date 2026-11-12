import { describe, expect, it } from 'vitest';
import { renderAboutText } from '@/lib/aboutText';

function dom(html: string): HTMLElement {
  const root = document.createElement('div');
  root.innerHTML = html;
  return root;
}

describe('renderAboutText: plain text', () => {
  it('splits blank-line separated paragraphs into <p> elements', () => {
    const root = dom(renderAboutText('First paragraph.\n\nSecond paragraph.', 'Placeholder'));
    const paragraphs = root.querySelectorAll('p');
    expect(paragraphs).toHaveLength(2);
    expect(paragraphs[0].textContent).toBe('First paragraph.');
    expect(paragraphs[1].textContent).toBe('Second paragraph.');
  });

  it('turns single newlines into <br>', () => {
    const root = dom(renderAboutText('Line one\nLine two', 'Placeholder'));
    expect(root.querySelectorAll('p')).toHaveLength(1);
    expect(root.querySelectorAll('br')).toHaveLength(1);
  });

  it('handles Windows line endings and extra blank lines', () => {
    const root = dom(renderAboutText('A\r\n\r\n\r\nB', 'Placeholder'));
    expect(root.querySelectorAll('p')).toHaveLength(2);
  });

  it('escapes angle brackets that are not tags', () => {
    const root = dom(renderAboutText('1 < 2 & 3 > 2', 'Placeholder'));
    expect(root.textContent).toBe('1 < 2 & 3 > 2');
    expect(root.children).toHaveLength(1);
  });

  it('falls back to an escaped placeholder paragraph when the text is empty', () => {
    expect(dom(renderAboutText('', 'Nothing yet <b>')).querySelector('p')?.textContent).toBe('Nothing yet <b>');
    expect(dom(renderAboutText('   \n ', 'Nothing yet')).querySelector('p')?.textContent).toBe('Nothing yet');
    expect(dom(renderAboutText(undefined, 'Nothing yet')).querySelector('p')?.textContent).toBe('Nothing yet');
  });
});

describe('renderAboutText: HTML input', () => {
  it('drops script elements together with their content', () => {
    const root = dom(renderAboutText('<script>alert(1)</script>Hi', 'Placeholder'));
    expect(root.querySelector('script')).toBeNull();
    expect(root.textContent).toBe('Hi');
  });

  it.each([
    ['iframe', '<iframe src="https://evil.example"></iframe>Hi'],
    ['style', '<style>body{display:none}</style>Hi'],
    ['img', '<img src="x" onerror="alert(1)">Hi'],
    ['svg', '<svg onload="alert(1)"><circle/></svg>Hi'],
    ['object', '<object data="x"></object>Hi'],
    ['form', '<form action="https://evil.example"><input></form>Hi'],
  ])('removes <%s>', (tag, html) => {
    const root = dom(renderAboutText(html, 'Placeholder'));
    expect(root.querySelector(tag)).toBeNull();
    expect(root.querySelector('[onerror],[onload]')).toBeNull();
    expect(root.textContent).toContain('Hi');
  });

  it.each([
    ['javascript:', 'javascript:alert(1)'],
    ['mixed case javascript:', 'JaVaScRiPt:alert(1)'],
    ['leading whitespace', '  javascript:alert(1)'],
    ['data:', 'data:text/html,<script>alert(1)</script>'],
    ['vbscript:', 'vbscript:x'],
    ['protocol-relative', '//evil.example'],
    ['relative', '/admin'],
  ])('strips href with %s', (_label, href) => {
    const root = dom(renderAboutText(`<a href="${href}">link</a>`, 'Placeholder'));
    const anchor = root.querySelector('a');
    expect(anchor?.getAttribute('href')).toBeNull();
    expect(anchor?.textContent).toBe('link');
  });

  it('keeps http(s) links and adds rel and target', () => {
    const root = dom(renderAboutText('<a href="https://example.com/x" onclick="x()" style="color:red" class="c">site</a>', 'P'));
    const anchor = root.querySelector('a')!;
    expect(anchor.getAttribute('href')).toBe('https://example.com/x');
    expect(anchor.getAttribute('rel')).toBe('noopener noreferrer');
    expect(anchor.getAttribute('target')).toBe('_blank');
    expect(anchor.getAttributeNames().sort()).toEqual(['href', 'rel', 'target']);
  });

  it('keeps mailto links without target', () => {
    const anchor = dom(renderAboutText('<a href="mailto:a@b.pl">mail</a>', 'P')).querySelector('a')!;
    expect(anchor.getAttribute('href')).toBe('mailto:a@b.pl');
    expect(anchor.getAttribute('target')).toBeNull();
  });

  it('drops on* handlers, style and class from allowed elements', () => {
    const root = dom(renderAboutText('<p onclick="x()" style="color:red" class="big" id="a">text</p>', 'P'));
    expect(root.querySelector('p')?.getAttributeNames()).toEqual([]);
  });

  it('keeps allowed formatting and lists', () => {
    const html = '<p><strong>b</strong> <b>b2</b> <em>e</em> <i>i</i> <u>u</u></p><ul><li>one</li></ul><ol><li>two</li></ol><h2>h2</h2><h3>h3</h3><h4>h4</h4>';
    const root = dom(renderAboutText(html, 'P'));
    for (const tag of ['strong', 'b', 'em', 'i', 'u', 'ul', 'ol', 'li', 'h2', 'h3', 'h4']) {
      expect(root.querySelector(tag), tag).not.toBeNull();
    }
    expect(root.querySelectorAll('li')).toHaveLength(2);
  });

  it('unwraps unknown elements but keeps their text', () => {
    const root = dom(renderAboutText('<div><span>kept</span> text</div>', 'P'));
    expect(root.querySelector('div,span')).toBeNull();
    expect(root.textContent).toBe('kept text');
  });

  it('sanitises nested disallowed content inside allowed elements', () => {
    const root = dom(renderAboutText('<ul><li><script>alert(1)</script>ok</li></ul>', 'P'));
    expect(root.querySelector('script')).toBeNull();
    expect(root.querySelector('li')?.textContent).toBe('ok');
  });
});
