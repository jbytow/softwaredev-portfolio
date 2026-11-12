import { motion } from 'framer-motion';
import { useTranslation } from 'react-i18next';
import Header from './Header';
import Footer from './Footer';
import RouteBoundary from './RouteBoundary';
import ScrollToTop from './ScrollToTop';

export default function Layout() {
  const { t } = useTranslation();

  return (
    <div className="min-h-screen flex flex-col">
      <ScrollToTop />
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-[60] btn-primary"
      >
        {t('common.skipToContent')}
      </a>
      <Header />
      <motion.main
        id="main"
        tabIndex={-1}
        className="flex-grow pt-16 md:pt-20 focus:outline-none"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
      >
        <RouteBoundary />
      </motion.main>
      <Footer />
    </div>
  );
}
