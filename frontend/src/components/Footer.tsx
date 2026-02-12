import { Link } from 'react-router-dom';
import { useNerdTranslation } from '@/hooks/useNerdTranslation';
import { useQuery } from '@tanstack/react-query';
import { Linkedin, Instagram, Facebook, Mail, Github } from 'lucide-react';
import { settingsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';

const socialIcons: Record<string, typeof Linkedin> = {
  linkedin: Linkedin,
  instagram: Instagram,
  facebook: Facebook,
  email: Mail,
  github: Github,
};

const footerLinks = [
  { path: '/about', labelKey: 'nav.about' },
  { path: '/experience', labelKey: 'nav.experience' },
  { path: '/projects', labelKey: 'nav.projects' },
  { path: '/skills', labelKey: 'nav.skills' },
  { path: '/contact', labelKey: 'nav.contact' },
];

export default function Footer() {
  const { t } = useNerdTranslation();
  const { language } = useLanguage();
  const currentYear = new Date().getFullYear();

  const { data: settingsData } = useQuery({
    queryKey: queryKeys.settings(language),
    queryFn: () => settingsApi.get(),
  });

  const settings = settingsData?.data;

  const getSocialHref = (platform: string, url: string) => {
    if (platform.toLowerCase() === 'email') {
      return `mailto:${url}`;
    }
    return url;
  };

  return (
    <footer className="bg-dark-900 border-t border-dark-800">
      <div className="container py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Brand */}
          <div className="space-y-4">
            <Link to="/" className="text-2xl font-display font-bold gradient-text">
              {settings?.footerTitle || settings?.heroTitle || 'Portfolio'}
            </Link>
            <p className="text-dark-400 text-sm max-w-xs">
              {settings?.footerTagline || settings?.heroSubtitle || t('footer.tagline')}
            </p>
          </div>

          {/* Quick Links */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-dark-100">{t('footer.quickLinks')}</h3>
            <ul className="space-y-2">
              {footerLinks.map((link) => (
                <li key={link.path}>
                  <Link
                    to={link.path}
                    className="text-dark-400 hover:text-primary-400 transition-colors text-sm"
                  >
                    {t(link.labelKey)}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Social Links */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-dark-100">{t('contact.info.social')}</h3>
            <div className="flex space-x-3">
              {settings?.socialLinks && Object.entries(settings.socialLinks).map(([platform, url]) => {
                const Icon = socialIcons[platform.toLowerCase()] || Linkedin;
                return (
                  <a
                    key={platform}
                    href={getSocialHref(platform, url)}
                    target={platform.toLowerCase() === 'email' ? undefined : '_blank'}
                    rel={platform.toLowerCase() === 'email' ? undefined : 'noopener noreferrer'}
                    className="p-2 text-dark-400 hover:text-primary-400 bg-dark-800 hover:bg-dark-700 rounded-lg transition-colors"
                    aria-label={platform}
                  >
                    <Icon className="w-5 h-5" />
                  </a>
                );
              })}
              {settings?.email && !settings?.socialLinks?.email && (
                <a
                  href={`mailto:${settings.email}`}
                  className="p-2 text-dark-400 hover:text-primary-400 bg-dark-800 hover:bg-dark-700 rounded-lg transition-colors"
                  aria-label="Email"
                >
                  <Mail className="w-5 h-5" />
                </a>
              )}
            </div>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="mt-12 pt-8 border-t border-dark-800 flex flex-col md:flex-row items-center justify-between space-y-4 md:space-y-0">
          <p className="text-dark-500 text-sm">
            &copy; {currentYear} {settings?.ownerName || 'Portfolio'}. {t('footer.rights')}.
          </p>
          <p className="text-dark-500 text-sm flex items-center flex-wrap justify-center gap-1">
            <a
              href="https://jbytow.pl"
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary-400 hover:text-primary-300 transition-colors"
            >
              Jakub Bytow
            </a>
            <span className="text-dark-600">|</span>
            <a
              href="https://github.com/jbytow"
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary-400 hover:text-primary-300 transition-colors flex items-center gap-1"
            >
              <Github className="w-4 h-4" />
              GitHub
            </a>
          </p>
        </div>
      </div>
    </footer>
  );
}
