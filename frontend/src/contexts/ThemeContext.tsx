import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { themePresets, ThemePreset } from '@/lib/themePresets';

interface ThemeContextType {
  theme: string;
  setTheme: (name: string) => void;
  cycleTheme: () => void;
  themes: ThemePreset[];
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

function applyTheme(preset: ThemePreset) {
  const style = document.documentElement.style;
  // Primary scale
  style.setProperty('--color-primary-50', preset.colors.primary50);
  style.setProperty('--color-primary-100', preset.colors.primary100);
  style.setProperty('--color-primary-200', preset.colors.primary200);
  style.setProperty('--color-primary-300', preset.colors.primary300);
  style.setProperty('--color-primary-400', preset.colors.primary400);
  style.setProperty('--color-primary-500', preset.colors.primary500);
  style.setProperty('--color-primary-600', preset.colors.primary600);
  style.setProperty('--color-primary-700', preset.colors.primary700);
  style.setProperty('--color-primary-800', preset.colors.primary800);
  style.setProperty('--color-primary-900', preset.colors.primary900);
  style.setProperty('--color-primary-950', preset.colors.primary950);
  style.setProperty('--color-accent', preset.colors.accent);
  // Dark (background) scale
  style.setProperty('--color-dark-50', preset.colors.dark50);
  style.setProperty('--color-dark-100', preset.colors.dark100);
  style.setProperty('--color-dark-200', preset.colors.dark200);
  style.setProperty('--color-dark-300', preset.colors.dark300);
  style.setProperty('--color-dark-400', preset.colors.dark400);
  style.setProperty('--color-dark-500', preset.colors.dark500);
  style.setProperty('--color-dark-600', preset.colors.dark600);
  style.setProperty('--color-dark-700', preset.colors.dark700);
  style.setProperty('--color-dark-800', preset.colors.dark800);
  style.setProperty('--color-dark-900', preset.colors.dark900);
  style.setProperty('--color-dark-950', preset.colors.dark950);
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<string>(
    () => localStorage.getItem('theme') || 'emerald'
  );

  useEffect(() => {
    const preset = themePresets.find((t) => t.name === theme) || themePresets[0];
    applyTheme(preset);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const setTheme = (name: string) => {
    setThemeState(name);
  };

  const cycleTheme = () => {
    setThemeState((prev) => {
      const idx = themePresets.findIndex((t) => t.name === prev);
      return themePresets[(idx + 1) % themePresets.length].name;
    });
  };

  return (
    <ThemeContext.Provider value={{ theme, setTheme, cycleTheme, themes: themePresets }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}
