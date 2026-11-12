import { useNerdTranslation } from '@/hooks/useNerdTranslation';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { Link, useSearchParams } from 'react-router-dom';
import { ArrowRight, X, Github, ExternalLink } from 'lucide-react';
import { postsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import { getMediaUrl } from '@/lib/mediaUrl';
import LoadingSpinner from '@/components/LoadingSpinner';
import HashtagList from '@/components/HashtagList';

export default function Projects() {
  const { t } = useNerdTranslation();
  const { language } = useLanguage();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeHashtag = searchParams.get('hashtag');

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: activeHashtag
      ? queryKeys.postsByHashtag(language, activeHashtag)
      : queryKeys.posts(language),
    queryFn: () =>
      activeHashtag
        ? postsApi.getAll(undefined, activeHashtag)
        : postsApi.getAll(),
  });

  const projects = data?.data || [];

  const clearHashtagFilter = () => {
    setSearchParams({});
  };

  if (isPending) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <>
      <Helmet>
        <title>{t('projects.title')} | Portfolio</title>
      </Helmet>

      <section className="py-20">
        <div className="container">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-16"
          >
            <h1 className="section-heading">{t('projects.title')}</h1>
            <p className="section-subheading mx-auto">{t('projects.subtitle')}</p>
          </motion.div>

          {/* Active hashtag filter */}
          {activeHashtag && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-8 flex items-center justify-center gap-2"
            >
              <span className="text-dark-400">{t('projects.filteredBy')}</span>
              <span className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary-500/20 text-primary-400">
                #{activeHashtag}
                <button
                  type="button"
                  onClick={clearHashtagFilter}
                  aria-label={t('projects.clearFilter')}
                  className="hover:text-primary-300 transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </span>
            </motion.div>
          )}

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {projects.map((project, index) => (
              <motion.article
                key={project.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                className="relative card-hover group has-[a:focus-visible]:ring-2 has-[a:focus-visible]:ring-primary-500"
              >
                {project.featuredImage && (
                  <div className="aspect-video rounded-xl overflow-hidden mb-4 bg-dark-700">
                    <img
                      src={getMediaUrl(project.featuredImage)}
                      alt={project.title}
                      className="w-full h-full object-cover pointer-events-none group-hover:scale-105 transition-transform duration-500"
                    />
                  </div>
                )}

                <h3 className="text-xl font-semibold text-dark-100 mb-2 group-hover:text-primary-400 transition-colors">
                  <Link
                    to={`/projects/${project.slug}`}
                    className="after:absolute after:inset-0 focus:outline-none"
                  >
                    {project.title}
                  </Link>
                </h3>

                <p className="text-dark-400 text-sm line-clamp-2 mb-4">{project.excerpt}</p>

                {project.hashtags && project.hashtags.length > 0 && (
                  <div className="mb-4">
                    <HashtagList
                      hashtags={project.hashtags}
                      clickable={true}
                      limit={3}
                      expandable={true}
                    />
                  </div>
                )}

                <div className="flex items-center gap-3">
                  <span className="inline-flex items-center text-primary-400 group-hover:text-primary-300 text-sm font-medium">
                    {t('projects.viewProject')}
                    <ArrowRight className="ml-1 w-4 h-4" />
                  </span>
                  <div className="relative z-10 flex gap-2 ml-auto">
                    {project.githubUrl && (
                      <a
                        href={project.githubUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="p-1.5 text-dark-400 hover:text-primary-400 bg-dark-700 hover:bg-dark-600 rounded-lg transition-colors"
                        title={t('projects.github')}
                        aria-label={t('projects.github')}
                      >
                        <Github className="w-4 h-4" />
                      </a>
                    )}
                    {project.liveUrl && (
                      <a
                        href={project.liveUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="p-1.5 text-dark-400 hover:text-accent bg-dark-700 hover:bg-dark-600 rounded-lg transition-colors"
                        title={t('projects.liveDemo')}
                        aria-label={t('projects.liveDemo')}
                      >
                        <ExternalLink className="w-4 h-4" />
                      </a>
                    )}
                  </div>
                </div>
              </motion.article>
            ))}
          </div>

          {isError && !data && (
            <div className="text-center py-12">
              <p className="text-dark-400 mb-4">{t('common.loadError')}</p>
              <button type="button" onClick={() => refetch()} className="btn-secondary">
                {t('common.retry')}
              </button>
            </div>
          )}

          {!isError && projects.length === 0 && (
            <div className="text-center text-dark-400 py-12">
              {activeHashtag
                ? t('projects.emptyForHashtag', { hashtag: activeHashtag })
                : t('projects.empty')}
            </div>
          )}
        </div>
      </section>
    </>
  );
}
