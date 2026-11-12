import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { MotionConfig } from 'framer-motion';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HelmetProvider } from 'react-helmet-async';
import { Toaster } from 'react-hot-toast';
import App from './App';
import { AuthProvider } from './contexts/AuthContext';
import { LanguageProvider } from './contexts/LanguageContext';
import { NerdModeProvider } from './contexts/NerdModeContext';
import { ThemeProvider } from './contexts/ThemeContext';
import { handlePreloadError } from './lib/chunkReload';
import './i18n';
import './styles/index.css';

window.addEventListener('vite:preloadError', handlePreloadError);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      retry: 1,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <MotionConfig reducedMotion="user">
      <HelmetProvider>
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <ThemeProvider>
              <LanguageProvider>
                <NerdModeProvider>
                <AuthProvider>
                  <App />
                  <Toaster
                    position="top-right"
                    toastOptions={{
                      duration: 4000,
                      style: {
                        background: '#1e293b',
                        color: '#f1f5f9',
                        border: '1px solid #334155',
                      },
                    }}
                  />
                </AuthProvider>
                </NerdModeProvider>
              </LanguageProvider>
            </ThemeProvider>
          </BrowserRouter>
        </QueryClientProvider>
    </HelmetProvider>
    </MotionConfig>
  </React.StrictMode>
);
