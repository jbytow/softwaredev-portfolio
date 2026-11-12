import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { themePresets, ThemePreset } from '@/lib/themePresets';

interface ThemeContextType {
  theme: string;
  setTheme: (name: string) => void;
  cycleTheme: () => void;
  themes: ThemePreset[];
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<string>(
    () => localStorage.getItem('theme') || 'emerald'
  );

  useEffect(() => {
    const preset = themePresets.find((t) => t.name === theme) || themePresets[0];
    document.documentElement.setAttribute('data-theme', preset.name);
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
