import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { motion, useScroll, useTransform, useInView } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { useRef, useEffect, useState } from 'react';
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
import { settingsApi, postsApi, achievementsApi } from '@/services/api';
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

function AnimatedCounter({ value, suffix = '' }: { value: number; suffix?: string }) {
  const ref = useRef<HTMLSpanElement>(null);
  const isInView = useInView(ref, { once: true });
  const [display, setDisplay] = useState(0);

  useEffect(() => {
    if (!isInView) return;
    const duration = 1500;
    const startTime = performance.now();

    function tick(now: number) {
      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setDisplay(Math.round(eased * value));
      if (progress < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
  }, [isInView, value]);

  return (
    <span ref={ref}>
      {display}{suffix}
    </span>
  );
}

function parseStatValue(val: string): { num: number; suffix: string } {
  const match = val.match(/^(\d+)(.*)$/);
  if (match) return { num: parseInt(match[1], 10), suffix: match[2] };
  return { num: 0, suffix: val };
}

export default function Home() {
  const { t } = useTranslation();
  const { language } = useLanguage();

  const { scrollY } = useScroll();
  const mountainBackY = useTransform(scrollY, [0, 600], [0, 60]);
  const mountainFrontY = useTransform(scrollY, [0, 600], [0, 100]);

  const { data: settingsData, isLoading: settingsLoading } = useQuery({
    queryKey: queryKeys.settings(language),
    queryFn: () => settingsApi.get(),
  });

  const { data: postsData, isLoading: postsLoading } = useQuery({
    queryKey: queryKeys.posts(language, 'featured'),
    queryFn: () => postsApi.getAll(),
  });

  const { data: achievementsData } = useQuery({
    queryKey: queryKeys.achievements(language),
    queryFn: () => achievementsApi.getAll(),
  });

  const settings = settingsData?.data;
  const featuredPosts = postsData?.data?.slice(0, 6) || [];
  const achievements = achievementsData?.data || [];

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
        <div className="absolute inset-0 bg-gradient-to-br from-primary-900/20 via-dark-900 to-accent/10" />
        <div className="absolute inset-0">
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary-600/20 rounded-full blur-3xl animate-pulse-slow" />
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-accent/20 rounded-full blur-3xl animate-pulse-slow delay-1000" />
        </div>

        {/* Mountain Silhouettes */}
        <motion.div
          className="absolute bottom-0 left-0 right-0 z-[1]"
          style={{ y: mountainBackY }}
        >
          <svg
            viewBox="0 0 1440 320"
            className="w-full h-auto"
            preserveAspectRatio="none"
          >
            <path
              d="M0,320 L0,220 Q120,120 240,180 Q360,240 480,160 Q540,120 600,140 Q720,200 840,130 Q960,60 1080,140 Q1200,220 1320,170 Q1380,140 1440,160 L1440,320 Z"
              className="fill-dark-900/20"
            />
          </svg>
        </motion.div>
        <motion.div
          className="absolute bottom-0 left-0 right-0 z-[2]"
          style={{ y: mountainFrontY }}
        >
          <svg
            viewBox="0 0 1440 240"
            className="w-full h-auto"
            preserveAspectRatio="none"
          >
            <path
              d="M0,240 L0,180 Q180,100 360,160 Q480,200 600,140 Q720,80 840,120 Q960,160 1080,100 Q1200,40 1320,100 Q1380,130 1440,120 L1440,240 Z"
              className="fill-dark-900/30"
            />
          </svg>
        </motion.div>

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
          className="absolute bottom-8 left-1/2 -translate-x-1/2 z-10"
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

      {/* Stats Section with HUD Frame */}
      {settings?.statsItems && settings.statsItems.length > 0 && (
        <section className="py-20 bg-dark-800/50">
          <div className="container">
            <div className="hud-frame hud-frame-bottom">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
                {settings.statsItems.map((stat, index) => {
                  const Icon = iconMap[stat.icon] || Target;
                  const { num, suffix } = parseStatValue(stat.value);
                  const barWidth = Math.min((num / 100) * 100, 100);
                  return (
                    <motion.div
                      key={index}
                      initial={{ opacity: 0, y: 20 }}
                      whileInView={{ opacity: 1, y: 0 }}
                      viewport={{ once: true }}
                      transition={{ delay: index * 0.1 }}
                      className="text-center"
                    >
                      <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl bg-gradient-to-br from-primary-500/20 to-accent/20 mb-4">
                        <Icon className="w-7 h-7 text-primary-400" />
                      </div>
                      <div className="text-3xl md:text-4xl font-bold gradient-text mb-2">
                        {num > 0 ? <AnimatedCounter value={num} suffix={suffix} /> : stat.value}
                      </div>
                      <div className="text-dark-400 text-sm">{stat.label}</div>
                      <div className="hud-stat-bar">
                        <motion.div
                          className="hud-stat-bar-fill"
                          initial={{ width: 0 }}
                          whileInView={{ width: `${barWidth}%` }}
                          viewport={{ once: true }}
                          transition={{ duration: 1.5, delay: index * 0.1 }}
                        />
                      </div>
                    </motion.div>
                  );
                })}
              </div>
            </div>
          </div>
        </section>
      )}

      {/* Achievements Section */}
      {achievements.length > 0 && (
        <section className="py-20">
          <div className="container">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              className="text-center mb-12"
            >
              <h2 className="section-heading">{t('home.achievements.title')}</h2>
              <p className="section-subheading mx-auto">{t('home.achievements.subtitle')}</p>
            </motion.div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {achievements.map((achievement, index) => {
                const Icon = iconMap[achievement.icon || ''] || Trophy;
                return (
                  <motion.div
                    key={achievement.id}
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: index * 0.1 }}
                    className="achievement-card"
                  >
                    <div className="flex items-start gap-4">
                      <div className="achievement-badge flex-shrink-0">
                        <Icon className="w-7 h-7 text-primary-400 relative z-10" />
                      </div>
                      <div className="min-w-0">
                        <h3 className="text-lg font-semibold text-dark-100 mb-1">
                          {achievement.title}
                        </h3>
                        <p className="text-dark-400 text-sm">
                          {achievement.description}
                        </p>
                        {achievement.year && (
                          <span className="inline-block mt-2 text-xs text-primary-400 font-mono">
                            {achievement.year}
                          </span>
                        )}
                      </div>
                    </div>
                  </motion.div>
                );
              })}
            </div>
          </div>
        </section>
      )}

      {/* Featured Work Section */}
      <section className="py-20 bg-dark-800/50">
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
      <section className="py-20 bg-gradient-to-r from-primary-900/50 to-accent/30">
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
