import { useState, useEffect } from 'react';
import { useNerdTranslation } from '@/hooks/useNerdTranslation';
import { motion, AnimatePresence } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { Dices } from 'lucide-react';
import {
  Zap,
  Rocket,
  Award,
  Database,
  Sparkles,
  Trophy,
  type LucideIcon,
} from 'lucide-react';
import { settingsApi, interestsApi, achievementsApi, rpgStatsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import { getMediaUrl } from '@/lib/mediaUrl';
import LoadingSpinner from '@/components/LoadingSpinner';

const questIconMap: Record<string, LucideIcon> = {
  zap: Zap,
  rocket: Rocket,
  award: Award,
  database: Database,
  sparkles: Sparkles,
  trophy: Trophy,
};

export default function About() {
  const { t } = useNerdTranslation();
  const { language } = useLanguage();
  const [rpgMode, setRpgMode] = useState(() => {
    return localStorage.getItem('rpgMode') === 'true';
  });

  useEffect(() => {
    localStorage.setItem('rpgMode', String(rpgMode));
  }, [rpgMode]);

  const { data: settingsData, isLoading: settingsLoading } = useQuery({
    queryKey: queryKeys.settings(language),
    queryFn: () => settingsApi.get(),
  });

  const { data: interestsData } = useQuery({
    queryKey: queryKeys.interests(language),
    queryFn: () => interestsApi.getAll(),
  });

  const { data: achievementsData } = useQuery({
    queryKey: queryKeys.achievements(language),
    queryFn: () => achievementsApi.getAll(),
  });

  const { data: rpgStatsData } = useQuery({
    queryKey: queryKeys.rpgStats(language),
    queryFn: () => rpgStatsApi.getAll(),
  });

  const settings = settingsData?.data;
  const interests = interestsData?.data || [];
  const achievements = achievementsData?.data || [];
  const rpgStats = rpgStatsData?.data || [];

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
            <div className="flex items-center justify-center gap-4 mb-4">
              <h1 className="section-heading !mb-0">{t('about.title')}</h1>
              <button
                onClick={() => setRpgMode(!rpgMode)}
                className={`rpg-toggle ${rpgMode ? 'active' : ''}`}
                title={t('about.rpg.toggle')}
              >
                <Dices className="w-5 h-5" />
                <span className="hidden sm:inline">{t('about.rpg.toggle')}</span>
              </button>
            </div>
            <p className="section-subheading mx-auto">{t('about.subtitle')}</p>
          </motion.div>

          <AnimatePresence mode="wait">
            {rpgMode ? (
              <motion.div
                key="rpg"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                transition={{ duration: 0.3 }}
              >
                <div className="rpg-parchment max-w-4xl mx-auto">
                  {/* Character Name Plate */}
                  <div className="text-center mb-8">
                    <h2 className="text-3xl md:text-4xl font-display font-bold gradient-text mb-2">
                      {settings?.heroTitle || 'Jakub Bytow'}
                    </h2>
                    <p className="text-dark-400 italic text-lg">
                      {settings?.rpgClassTitle || t('about.rpg.classTitle')}
                    </p>
                  </div>

                  {/* Stats Grid */}
                  {rpgStats.length > 0 && (
                    <div className="mb-8">
                      <h3 className="text-xl font-display font-bold text-dark-200 mb-4">
                        {t('about.rpg.statsTitle')}
                      </h3>
                      <div className="grid md:grid-cols-2 gap-4">
                        {rpgStats.map((stat, index) => (
                          <motion.div
                            key={stat.id}
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: index * 0.1 }}
                            className="p-4 rounded-xl bg-dark-900/40 border border-dark-700/50"
                          >
                            <div className="flex items-center justify-between mb-2">
                              <div className="flex items-center gap-3">
                                <span className="text-lg font-mono font-bold text-primary-400">
                                  {stat.attr}
                                </span>
                                <span className="text-dark-200 font-medium">
                                  {stat.label}
                                </span>
                              </div>
                              <span className="text-sm font-mono text-dark-400">
                                {stat.level}/{stat.maxLevel}
                              </span>
                            </div>
                            <div className="rpg-stat-bar mb-2">
                              <motion.div
                                className="rpg-stat-fill"
                                initial={{ width: 0 }}
                                animate={{ width: `${(stat.level / stat.maxLevel) * 100}%` }}
                                transition={{ duration: 1, delay: index * 0.1 + 0.3 }}
                              />
                            </div>
                            <div className="flex flex-wrap gap-1.5">
                              {stat.skills.map((skill) => (
                                <span
                                  key={skill}
                                  className="text-xs px-2 py-0.5 rounded bg-dark-700/50 text-dark-400"
                                >
                                  {skill}
                                </span>
                              ))}
                            </div>
                          </motion.div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Quests Completed */}
                  {achievements.length > 0 && (
                    <div>
                      <h3 className="text-xl font-display font-bold text-dark-200 mb-4">
                        {t('about.rpg.questsTitle')}
                      </h3>
                      <div className="space-y-3">
                        {achievements.map((achievement, index) => {
                          const Icon = questIconMap[achievement.icon || ''] || Trophy;
                          return (
                            <motion.div
                              key={achievement.id}
                              initial={{ opacity: 0, x: -20 }}
                              animate={{ opacity: 1, x: 0 }}
                              transition={{ delay: index * 0.08 + 0.6 }}
                              className="quest-badge"
                            >
                              <div className="quest-glow flex-shrink-0">
                                <Icon className="w-5 h-5 text-primary-400" />
                              </div>
                              <div className="min-w-0 flex-1">
                                <div className="font-medium text-dark-200">
                                  {achievement.title}
                                </div>
                                <div className="text-sm text-dark-400">
                                  {achievement.description}
                                </div>
                              </div>
                              {achievement.year && (
                                <span className="text-xs font-mono text-primary-400/70 flex-shrink-0">
                                  {achievement.year}
                                </span>
                              )}
                            </motion.div>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>
              </motion.div>
            ) : (
              <motion.div
                key="normal"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                transition={{ duration: 0.3 }}
              >
                <div className="grid md:grid-cols-2 gap-12 items-center">
                  <motion.div
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: 0.2 }}
                    className="relative"
                  >
                    {settings?.profileImage ? (
                      <div className="relative">
                        <div className="absolute inset-0 bg-gradient-to-br from-primary-500 to-accent rounded-2xl transform rotate-3" />
                        <img
                          src={settings.profileImage}
                          alt="Profile"
                          className="relative rounded-2xl w-full object-cover shadow-xl"
                        />
                      </div>
                    ) : (
                      <div className="aspect-square rounded-2xl bg-gradient-to-br from-primary-500/20 to-accent/20 flex items-center justify-center">
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
              </motion.div>
            )}
          </AnimatePresence>
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
