import { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface NerdModeContextType {
  nerdMode: boolean;
  setNerdMode: (enabled: boolean) => void;
  toggleNerdMode: () => void;
}

const NerdModeContext = createContext<NerdModeContextType | undefined>(undefined);

export function NerdModeProvider({ children }: { children: ReactNode }) {
  const [nerdMode, setNerdModeState] = useState<boolean>(
    () => localStorage.getItem('nerdMode') === 'true'
  );

  useEffect(() => {
    localStorage.setItem('nerdMode', String(nerdMode));
  }, [nerdMode]);

  const setNerdMode = (enabled: boolean) => {
    setNerdModeState(enabled);
  };

  const toggleNerdMode = () => {
    setNerdModeState((prev) => !prev);
  };

  return (
    <NerdModeContext.Provider value={{ nerdMode, setNerdMode, toggleNerdMode }}>
      {children}
    </NerdModeContext.Provider>
  );
}

export function useNerdMode() {
  const context = useContext(NerdModeContext);
  if (context === undefined) {
    throw new Error('useNerdMode must be used within a NerdModeProvider');
  }
  return context;
}
