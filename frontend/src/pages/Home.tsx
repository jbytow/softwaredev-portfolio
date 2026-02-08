import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import {
  ArrowRight,
  Sparkles,
  Target,
  Users,
  TrendingUp,
  Lightbulb,
  MessageSquare,
  Clock,
  Heart,
  Zap,
  Brain,
  Handshake,
  Rocket,
  Award,
  Star,
  Trophy,
  Briefcase,
  Globe,
  Code,
  Terminal,
  Database,
  Server,
  GitBranch,
  Monitor,
  type LucideIcon,
} from 'lucide-react';
import { Helmet } from 'react-helmet-async';
import { settingsApi, postsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import { getMediaUrl } from '@/lib/mediaUrl';
import LoadingSpinner from '@/components/LoadingSpinner';

const iconMap: Record<string, LucideIcon> = {
  target: Target,
  users: Users,
  'trending-up': TrendingUp,
  sparkles: Sparkles,
  lightbulb: Lightbulb,
  message: MessageSquare,
  clock: Clock,
  heart: Heart,
  zap: Zap,
  brain: Brain,
  handshake: Handshake,
  rocket: Rocket,
  award: Award,
  star: Star,
  trophy: Trophy,
  briefcase: Briefcase,
  globe: Globe,
  code: Code,
  terminal: Terminal,
  database: Database,
  server: Server,
  'git-branch': GitBranch,
  monitor: Monitor,
};

export default function Home() {
  const { t } = useTranslation();
  const { language } = useLanguage();

  const { data: settingsData, isLoading: settingsLoading } = useQuery({
    queryKey: queryKeys.settings(language),
    queryFn: () => settingsApi.get(),
  });

  const { data: postsData, isLoading: postsLoading } = useQuery({
    queryKey: queryKeys.posts(language, 'featured'),
    queryFn: () => postsApi.getAll(),
  });

  const settings = settingsData?.data;
  const featuredPosts = postsData?.data?.slice(0, 6) || [];

  if (settingsLoading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <>
      <Helmet>
        <title>{settings?.heroTitle || 'Dev Portfolio'}</title>
        <meta name="description" content={settings?.metaDescription || ''} />
      </Helmet>

      {/* Hero Section */}
      <section className="relative min-h-[calc(100vh-5rem)] flex items-center overflow-hidden">
        {/* Animated Background */}
        <div className="absolute inset-0 bg-gradient-to-br from-primary-900/20 via-dark-900 to-accent-lime/10" />
        <div className="absolute inset-0">
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary-600/20 rounded-full blur-3xl animate-pulse-slow" />
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-accent-lime/20 rounded-full blur-3xl animate-pulse-slow delay-1000" />
        </div>

        <div className="container relative z-10">
          <div className="max-w-4xl mx-auto text-center">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.05 }}
              className="text-5xl mb-4"
            >
              👋
            </motion.div>
            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
              className="text-primary-400 font-medium mb-4"
            >
              {t('home.hero.greeting')}
            </motion.p>

            <motion.h1
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="text-5xl md:text-7xl font-display font-bold mb-6"
            >
              <span className="gradient-text">{settings?.heroTitle}</span>
            </motion.h1>

            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
              className="text-xl md:text-2xl text-dark-300 mb-10 max-w-2xl mx-auto"
            >
              {settings?.heroSubtitle || t('home.hero.tagline')}
            </motion.p>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
              className="flex flex-col sm:flex-row items-center justify-center gap-4"
            >
              <Link to="/projects" className="btn-primary">
                {t('home.hero.cta')}
                <ArrowRight className="ml-2 w-5 h-5" />
              </Link>
              <Link to="/contact" className="btn-secondary">
                {t('common.contactMe')}
              </Link>
            </motion.div>
          </div>
        </div>

        {/* Scroll Indicator */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1 }}
          className="absolute bottom-8 left-1/2 -translate-x-1/2"
        >
          <div className="w-6 h-10 rounded-full border-2 border-dark-600 flex justify-center pt-2">
            <motion.div
              animate={{ y: [0, 12, 0] }}
              transition={{ repeat: Infinity, duration: 1.5 }}
              className="w-1.5 h-1.5 bg-primary-400 rounded-full"
            />
          </div>
        </motion.div>
      </section>

      {/* Stats Section */}
      {settings?.statsItems && settings.statsItems.length > 0 && (
        <section className="py-20 bg-dark-800/50">
          <div className="container">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
              {settings.statsItems.map((stat, index) => {
                const Icon = iconMap[stat.icon] || Target;
                return (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: index * 0.1 }}
                    className="text-center"
                  >
                    <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl bg-gradient-to-br from-primary-500/20 to-accent-lime/20 mb-4">
                      <Icon className="w-7 h-7 text-primary-400" />
                    </div>
                    <div className="text-3xl md:text-4xl font-bold gradient-text mb-2">
                      {stat.value}
                    </div>
                    <div className="text-dark-400 text-sm">{stat.label}</div>
                  </motion.div>
                );
              })}
            </div>
          </div>
        </section>
      )}

      {/* Featured Work Section */}
      <section className="py-20">
        <div className="container">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-12"
          >
            <h2 className="section-heading">{t('home.featured.title')}</h2>
            <p className="section-subheading mx-auto">{t('home.featured.subtitle')}</p>
          </motion.div>

          {postsLoading ? (
            <LoadingSpinner />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {featuredPosts.map((post, index) => (
                <motion.article
                  key={post.id}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: index * 0.1 }}
                  className="card-hover group"
                >
                  {post.featuredImage && (
                    <div className="aspect-video rounded-xl overflow-hidden mb-4 bg-dark-700">
                      <img
                        src={getMediaUrl(post.featuredImage)}
                        alt={post.title}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      />
                    </div>
                  )}
                  <span className="badge-primary mb-3">{post.categoryLabel}</span>
                  <h3 className="text-xl font-semibold text-dark-100 mb-2 group-hover:text-primary-400 transition-colors">
                    {post.title}
                  </h3>
                  <p className="text-dark-400 text-sm line-clamp-2">{post.excerpt}</p>
                  <Link
                    to={`/projects/${post.slug}`}
                    className="inline-flex items-center mt-4 text-primary-400 hover:text-primary-300 text-sm font-medium"
                  >
                    {t('common.readMore')}
                    <ArrowRight className="ml-1 w-4 h-4" />
                  </Link>
                </motion.article>
              ))}
            </div>
          )}

          <motion.div
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            className="text-center mt-12"
          >
            <Link to="/projects" className="btn-outline">
              {t('common.viewMore')}
              <ArrowRight className="ml-2 w-5 h-5" />
            </Link>
          </motion.div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-primary-900/50 to-accent-lime/30">
        <div className="container text-center">
          <motion.h2
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-3xl md:text-4xl font-display font-bold text-dark-100 mb-6"
          >
            {t('home.cta.title')}
          </motion.h2>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.1 }}
            className="text-dark-300 mb-8 max-w-xl mx-auto"
          >
            {t('home.cta.subtitle')}
          </motion.p>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.2 }}
          >
            <Link to="/contact" className="btn-primary">
              {t('common.contactMe')}
              <ArrowRight className="ml-2 w-5 h-5" />
            </Link>
          </motion.div>
        </div>
      </section>
    </>
  );
}
