import { useState } from 'react';
import { useNerdTranslation } from '@/hooks/useNerdTranslation';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import toast from 'react-hot-toast';
import {
  Mail,
  Phone,
  MapPin,
  Send,
  Linkedin,
  Instagram,
  Facebook,
  Github,
} from 'lucide-react';
import { settingsApi, contactApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

const socialIcons: Record<string, typeof Linkedin> = {
  linkedin: Linkedin,
  instagram: Instagram,
  facebook: Facebook,
  github: Github,
};

export default function Contact() {
  const { t } = useNerdTranslation();
  const { language } = useLanguage();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
  });

  const { data: settingsData, isLoading } = useQuery({
    queryKey: queryKeys.settings(language),
    queryFn: () => settingsApi.get(),
  });

  const settings = settingsData?.data;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await contactApi.submit(formData);
      toast.success(t('contact.form.success'));
      setFormData({ name: '', email: '', subject: '', message: '' });
    } catch {
      toast.error(t('contact.form.error'));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <>
      <Helmet>
        <title>{t('contact.title')} | Portfolio</title>
      </Helmet>

      <section className="py-20">
        <div className="container">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-16"
          >
            <h1 className="section-heading">{t('contact.title')}</h1>
            <p className="section-subheading mx-auto">{t('contact.subtitle')}</p>
          </motion.div>

          <div className="grid lg:grid-cols-2 gap-12 max-w-6xl mx-auto">
            {/* Contact Form */}
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.1 }}
            >
              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid sm:grid-cols-2 gap-6">
                  <div>
                    <label htmlFor="name" className="label">
                      {t('contact.form.name')}
                    </label>
                    <input
                      type="text"
                      id="name"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      className="input"
                      required
                    />
                  </div>
                  <div>
                    <label htmlFor="email" className="label">
                      {t('contact.form.email')}
                    </label>
                    <input
                      type="email"
                      id="email"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      className="input"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="subject" className="label">
                    {t('contact.form.subject')}
                  </label>
                  <input
                    type="text"
                    id="subject"
                    value={formData.subject}
                    onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                    className="input"
                    required
                  />
                </div>

                <div>
                  <label htmlFor="message" className="label">
                    {t('contact.form.message')}
                  </label>
                  <textarea
                    id="message"
                    rows={6}
                    value={formData.message}
                    onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                    className="input resize-none"
                    required
                  />
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isSubmitting ? (
                    t('contact.form.sending')
                  ) : (
                    <>
                      {t('contact.form.send')}
                      <Send className="ml-2 w-5 h-5" />
                    </>
                  )}
                </button>
              </form>
            </motion.div>

            {/* Contact Info */}
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.2 }}
              className="space-y-8"
            >
              <div className="card">
                <h3 className="text-xl font-semibold text-dark-100 mb-6">
                  Contact Information
                </h3>

                <div className="space-y-4">
                  {settings?.email && (
                    <a
                      href={`mailto:${settings.email}`}
                      className="flex items-center text-dark-300 hover:text-primary-400 transition-colors"
                    >
                      <div className="w-10 h-10 rounded-lg bg-dark-700 flex items-center justify-center mr-4">
                        <Mail className="w-5 h-5 text-primary-400" />
                      </div>
                      <div>
                        <div className="text-sm text-dark-500">{t('contact.info.email')}</div>
                        <div>{settings.email}</div>
                      </div>
                    </a>
                  )}

                  {settings?.phone && (
                    <a
                      href={`tel:${settings.phone}`}
                      className="flex items-center text-dark-300 hover:text-primary-400 transition-colors"
                    >
                      <div className="w-10 h-10 rounded-lg bg-dark-700 flex items-center justify-center mr-4">
                        <Phone className="w-5 h-5 text-primary-400" />
                      </div>
                      <div>
                        <div className="text-sm text-dark-500">{t('contact.info.phone')}</div>
                        <div>{settings.phone}</div>
                      </div>
                    </a>
                  )}

                  <div className="flex items-center text-dark-300">
                    <div className="w-10 h-10 rounded-lg bg-dark-700 flex items-center justify-center mr-4">
                      <MapPin className="w-5 h-5 text-primary-400" />
                    </div>
                    <div>
                      <div className="text-sm text-dark-500">{t('contact.info.location')}</div>
                      <div>Remote / Worldwide</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Social Links */}
              {settings?.socialLinks && Object.keys(settings.socialLinks).length > 0 && (
                <div className="card">
                  <h3 className="text-xl font-semibold text-dark-100 mb-6">
                    {t('contact.info.social')}
                  </h3>

                  <div className="flex flex-wrap gap-3">
                    {Object.entries(settings.socialLinks).map(([platform, url]) => {
                      const Icon = socialIcons[platform.toLowerCase()] || Linkedin;
                      return (
                        <a
                          key={platform}
                          href={url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="w-12 h-12 rounded-xl bg-dark-700 hover:bg-dark-600 flex items-center justify-center text-dark-400 hover:text-primary-400 transition-colors"
                          aria-label={platform}
                        >
                          <Icon className="w-5 h-5" />
                        </a>
                      );
                    })}
                  </div>
                </div>
              )}
            </motion.div>
          </div>
        </div>
      </section>
    </>
  );
}
