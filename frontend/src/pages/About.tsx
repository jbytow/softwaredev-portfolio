import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { settingsApi, interestsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import { getMediaUrl } from '@/lib/mediaUrl';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function About() {
  const { t } = useTranslation();
  const { language } = useLanguage();

  const { data: settingsData, isLoading: settingsLoading } = useQuery({
    queryKey: queryKeys.settings(language),
    queryFn: () => settingsApi.get(),
  });

  const { data: interestsData } = useQuery({
    queryKey: queryKeys.interests(language),
    queryFn: () => interestsApi.getAll(),
  });

  const settings = settingsData?.data;
  const interests = interestsData?.data || [];

  if (settingsLoading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <>
      <Helmet>
        <title>{t('about.title')} | Portfolio</title>
      </Helmet>

      <section className="py-20">
        <div className="container">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-16"
          >
            <h1 className="section-heading">{t('about.title')}</h1>
            <p className="section-subheading mx-auto">{t('about.subtitle')}</p>
          </motion.div>

          <div className="grid md:grid-cols-2 gap-12 items-center">
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.2 }}
              className="relative"
            >
              {settings?.profileImage ? (
                <div className="relative">
                  <div className="absolute inset-0 bg-gradient-to-br from-primary-500 to-accent-lime rounded-2xl transform rotate-3" />
                  <img
                    src={settings.profileImage}
                    alt="Profile"
                    className="relative rounded-2xl w-full object-cover shadow-xl"
                  />
                </div>
              ) : (
                <div className="aspect-square rounded-2xl bg-gradient-to-br from-primary-500/20 to-accent-lime/20 flex items-center justify-center">
                  <span className="text-6xl">👋</span>
                </div>
              )}
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
              className="space-y-6"
            >
              <div
                className="prose prose-invert prose-lg max-w-none"
                dangerouslySetInnerHTML={{
                  __html: settings?.aboutText || '<p>About content coming soon...</p>',
                }}
              />

              {settings?.aboutTags && settings.aboutTags.length > 0 && (
                <div className="flex flex-wrap gap-3 pt-4">
                  {settings.aboutTags.map((tag) => (
                    <span key={tag} className="badge-primary">
                      {tag}
                    </span>
                  ))}
                </div>
              )}
            </motion.div>
          </div>
        </div>
      </section>

      {/* Interests Section */}
      {interests.length > 0 && (
        <section className="py-20 bg-dark-800/50">
          <div className="container">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              className="text-center mb-12"
            >
              <h2 className="section-heading">{t('about.interests.title')}</h2>
              <p className="section-subheading mx-auto">{t('about.interests.subtitle')}</p>
            </motion.div>

            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
              {interests.map((interest, index) => {
                const images = [interest.image1, interest.image2, interest.image3].filter(Boolean);
                return (
                  <motion.div
                    key={interest.id}
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: index * 0.1 }}
                    className="card"
                  >
                    <h3 className="text-xl font-semibold text-dark-100 mb-4">{interest.title}</h3>
                    {images.length > 0 && (
                      <div className={`grid gap-2 ${images.length === 1 ? 'grid-cols-1' : images.length === 2 ? 'grid-cols-2' : 'grid-cols-3'}`}>
                        {images.map((img, idx) => (
                          <div
                            key={idx}
                            className={`aspect-square rounded-lg overflow-hidden ${images.length === 1 ? 'aspect-video' : ''}`}
                          >
                            <img
                              src={getMediaUrl(img!)}
                              alt={`${interest.title} ${idx + 1}`}
                              className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                            />
                          </div>
                        ))}
                      </div>
                    )}
                  </motion.div>
                );
              })}
            </div>
          </div>
        </section>
      )}
    </>
  );
}
