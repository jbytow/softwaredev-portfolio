export interface ThemePreset {
  name: string;
  label: string;
  colors: {
    primary50: string;
    primary100: string;
    primary200: string;
    primary300: string;
    primary400: string;
    primary500: string;
    primary600: string;
    primary700: string;
    primary800: string;
    primary900: string;
    primary950: string;
    accent: string;
    dark50: string;
    dark100: string;
    dark200: string;
    dark300: string;
    dark400: string;
    dark500: string;
    dark600: string;
    dark700: string;
    dark800: string;
    dark900: string;
    dark950: string;
  };
}

export const themePresets: ThemePreset[] = [
  // ── 1. Emerald & Lime ─────────────────────────────────────────
  // Fresh, modern "dev/hacker" vibe. Neutral gray backgrounds.
  {
    name: 'emerald',
    label: 'Emerald & Lime',
    colors: {
      primary50:  '236 253 245',
      primary100: '209 250 229',
      primary200: '167 243 208',
      primary300: '110 231 183',
      primary400: '52 211 153',
      primary500: '16 185 129',
      primary600: '5 150 105',
      primary700: '4 120 87',
      primary800: '6 95 70',
      primary900: '6 78 59',
      primary950: '2 44 34',
      accent:     '132 204 22',   // lime-500
      dark50:  '250 250 250',
      dark100: '245 245 245',
      dark200: '229 229 229',
      dark300: '212 212 212',
      dark400: '163 163 163',
      dark500: '115 115 115',
      dark600: '82 82 82',
      dark700: '64 64 64',
      dark800: '38 38 38',
      dark900: '23 23 23',
      dark950: '10 10 10',
    },
  },

  // ── A. Coffee & Code ───────────────────────────────────────────
  // Warm espresso/charcoal backgrounds, amber primary, cream accent.
  {
    name: 'coffee',
    label: 'Coffee & Code',
    colors: {
      // Primary: amber (warm coffee-brown tones)
      primary50:  '255 251 235',
      primary100: '254 243 199',
      primary200: '253 230 138',
      primary300: '252 211 77',
      primary400: '251 191 36',
      primary500: '245 158 11',
      primary600: '217 119 6',   // #d97706
      primary700: '180 83 9',
      primary800: '146 64 14',   // #92400e
      primary900: '120 53 15',
      primary950: '69 26 3',
      accent:     '254 243 199', // cream (#fef3c7)
      // Dark: stone (warm charcoal/espresso)
      dark50:  '250 250 249',
      dark100: '245 245 244',
      dark200: '231 229 228',
      dark300: '214 211 209',
      dark400: '168 162 158',
      dark500: '120 113 108',
      dark600: '87 83 78',
      dark700: '68 64 60',
      dark800: '41 37 36',
      dark900: '28 25 23',
      dark950: '12 10 9',
    },
  },

  // ── B. Mountain Dusk ───────────────────────────────────────────
  // Deep slate backgrounds, muted forest green, warm amber accent.
  {
    name: 'mountain',
    label: 'Mountain Dusk',
    colors: {
      // Primary: emerald (muted forest green)
      primary50:  '236 253 245',
      primary100: '209 250 229',
      primary200: '167 243 208',
      primary300: '110 231 183',
      primary400: '52 211 153',
      primary500: '16 185 129',
      primary600: '5 150 105',   // #059669
      primary700: '4 120 87',
      primary800: '6 95 70',
      primary900: '6 78 59',
      primary950: '2 44 34',
      accent:     '217 119 6',   // warm amber #d97706
      // Dark: slate
      dark50:  '248 250 252',
      dark100: '241 245 249',
      dark200: '226 232 240',
      dark300: '203 213 225',
      dark400: '148 163 184',
      dark500: '100 116 139',
      dark600: '71 85 105',
      dark700: '51 65 85',
      dark800: '30 41 59',
      dark900: '15 23 42',
      dark950: '2 6 23',
    },
  },

  // ── C. Terminal Classic ────────────────────────────────────────
  // Near-black backgrounds, green phosphor accent. Clean, no-nonsense.
  {
    name: 'terminal',
    label: 'Terminal Classic',
    colors: {
      // Primary: emerald (phosphor green)
      primary50:  '236 253 245',
      primary100: '209 250 229',
      primary200: '167 243 208',
      primary300: '110 231 183',
      primary400: '52 211 153',   // #34d399 (bright phosphor)
      primary500: '16 185 129',   // #10b981
      primary600: '5 150 105',
      primary700: '4 120 87',
      primary800: '6 95 70',
      primary900: '6 78 59',
      primary950: '2 44 34',
      accent:     '74 222 128',   // green-400 #4ade80 (bright CRT glow)
      // Dark: zinc (near-black)
      dark50:  '250 250 250',
      dark100: '244 244 245',
      dark200: '228 228 231',
      dark300: '212 212 216',
      dark400: '161 161 170',
      dark500: '113 113 122',
      dark600: '82 82 91',
      dark700: '63 63 70',
      dark800: '39 39 42',
      dark900: '24 24 27',
      dark950: '9 9 11',
    },
  },

  // ── D. Neon Quest ──────────────────────────────────────────────
  // Deep purple-black, electric violet primary, gold accent. RPG/gaming.
  {
    name: 'neon',
    label: 'Neon Quest',
    colors: {
      // Primary: violet (electric purple)
      primary50:  '245 243 255',
      primary100: '237 233 254',
      primary200: '221 214 254',
      primary300: '196 181 253',
      primary400: '167 139 250',
      primary500: '139 92 246',   // #8b5cf6
      primary600: '124 58 237',
      primary700: '109 40 217',
      primary800: '91 33 182',
      primary900: '76 29 149',
      primary950: '46 16 101',
      accent:     '245 158 11',   // gold #f59e0b
      // Dark: custom purple-black
      dark50:  '245 243 248',
      dark100: '235 232 240',
      dark200: '208 202 219',
      dark300: '176 165 192',
      dark400: '139 127 163',
      dark500: '102 90 133',
      dark600: '76 64 107',
      dark700: '56 45 82',
      dark800: '39 29 61',
      dark900: '26 16 41',
      dark950: '15 10 26',
    },
  },

  // ── 2. Violet & Amber ─────────────────────────────────────────
  // Bold, creative, high-contrast. Zinc backgrounds.
  {
    name: 'violet',
    label: 'Violet & Amber',
    colors: {
      primary50:  '245 243 255',
      primary100: '237 233 254',
      primary200: '221 214 254',
      primary300: '196 181 253',
      primary400: '167 139 250',
      primary500: '139 92 246',   // #8b5cf6
      primary600: '124 58 237',   // #7c3aed
      primary700: '109 40 217',
      primary800: '91 33 182',
      primary900: '76 29 149',
      primary950: '46 16 101',
      accent:     '245 158 11',   // amber #f59e0b
      // Dark: zinc
      dark50:  '250 250 250',
      dark100: '244 244 245',
      dark200: '228 228 231',
      dark300: '212 212 216',
      dark400: '161 161 170',
      dark500: '113 113 122',
      dark600: '82 82 91',
      dark700: '63 63 70',
      dark800: '39 39 42',
      dark900: '24 24 27',
      dark950: '9 9 11',
    },
  },

  // ── 3. Cyan & Rose ─────────────────────────────────────────────
  // Cyberpunk/neon feel. Deep navy/black, cyan glow, rose accent.
  {
    name: 'cyber',
    label: 'Cyan & Rose',
    colors: {
      // Primary: cyan
      primary50:  '236 254 255',
      primary100: '207 250 254',
      primary200: '165 243 252',
      primary300: '103 232 249',
      primary400: '34 211 238',
      primary500: '6 182 212',    // #06b6d4
      primary600: '8 145 178',    // #0891b2
      primary700: '14 116 144',
      primary800: '21 94 117',
      primary900: '22 78 99',
      primary950: '8 51 68',
      accent:     '244 63 94',    // rose #f43f5e
      // Dark: slate (deep navy/black)
      dark50:  '248 250 252',
      dark100: '241 245 249',
      dark200: '226 232 240',
      dark300: '203 213 225',
      dark400: '148 163 184',
      dark500: '100 116 139',
      dark600: '71 85 105',
      dark700: '51 65 85',
      dark800: '30 41 59',
      dark900: '15 23 42',
      dark950: '2 6 23',
    },
  },
];
