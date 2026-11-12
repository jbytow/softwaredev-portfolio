import { useState } from 'react';
import { useNerdTranslation } from '@/hooks/useNerdTranslation';
import { motion } from 'framer-motion';
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
import { contactApi } from '@/services/api';
import { useSiteSettings } from '@/lib/useSiteSettings';
import LoadingSpinner from '@/components/LoadingSpinner';

const socialIcons: Record<string, typeof Linkedin> = {
  linkedin: Linkedin,
  instagram: Instagram,
  facebook: Facebook,
  github: Github,
};

export default function Contact() {
  const { t } = useNerdTranslation();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
    website: '',
  });

  const { data: settingsData, isPending, isError, refetch } = useSiteSettings();

  const settings = settingsData?.data;
  const socialLinks = Object.entries(settings?.socialLinks ?? {}).filter(([, url]) => url?.trim());

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await contactApi.submit(formData);
      toast.success(t('contact.form.success'));
      setFormData({ name: '', email: '', subject: '', message: '', website: '' });
    } catch {
      toast.error(t('contact.form.error'));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isPending) {
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
                      maxLength={100}
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
                    maxLength={200}
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
                    maxLength={5000}
                    required
                  />
                </div>

                {/* Honeypot: bots fill every field; kept off-screen rather than display:none, which many bots skip */}
                <div aria-hidden="true" style={{ position: 'absolute', left: '-9999px' }}>
                  <label htmlFor="website">{t('contact.honeypot')}</label>
                  <input
                    type="text"
                    id="website"
                    name="website"
                    value={formData.website}
                    onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                    tabIndex={-1}
                    autoComplete="off"
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
                  {t('contact.info.title')}
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
                      <div>{t('contact.info.remote')}</div>
                    </div>
                  </div>
                </div>
              </div>

              {isError && !settingsData && (
                <div className="card text-center">
                  <p className="text-dark-400 mb-4">{t('common.loadError')}</p>
                  <button type="button" onClick={() => refetch()} className="btn-secondary">
                    {t('common.retry')}
                  </button>
                </div>
              )}

              {/* Social Links */}
              {socialLinks.length > 0 && (
                <div className="card">
                  <h3 className="text-xl font-semibold text-dark-100 mb-6">
                    {t('contact.info.social')}
                  </h3>

                  <div className="flex flex-wrap gap-3">
                    {socialLinks.map(([platform, url]) => {
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
