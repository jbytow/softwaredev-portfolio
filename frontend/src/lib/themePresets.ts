export interface ThemePreset {
  name: string;
  label: string;
}

// Colour values live in styles/index.css as html[data-theme='name'] blocks (emerald is :root),
// so the inline script in index.html can pick a preset by attribute alone before React starts.
export const themePresets: ThemePreset[] = [
  { name: 'emerald', label: 'Emerald & Lime' },
  { name: 'coffee', label: 'Coffee & Code' },
  { name: 'mountain', label: 'Mountain Dusk' },
  { name: 'terminal', label: 'Terminal Classic' },
  { name: 'neon', label: 'Neon Quest' },
  { name: 'violet', label: 'Violet & Amber' },
  { name: 'cyber', label: 'Cyan & Rose' },
];
