import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { ArrowLeft, ChevronDown, ChevronUp, Target, Lightbulb, TrendingUp, Quote, Github, ExternalLink } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { postsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import { getMediaUrl } from '@/lib/mediaUrl';
import LoadingSpinner from '@/components/LoadingSpinner';
import HashtagList from '@/components/HashtagList';
import MediaCarousel from '@/components/MediaCarousel';
import RichTextContent from '@/components/RichTextContent';

export default function PostDetail() {
  const { slug } = useParams<{ slug: string }>();
  const { language } = useLanguage();
  const { t } = useTranslation();
  const [caseStudyExpanded, setCaseStudyExpanded] = useState(false);

  const backPath = '/projects';
  const backLabelKey = 'projects.title';

  const { data, isLoading, error } = useQuery({
    queryKey: queryKeys.post(language, slug!),
    queryFn: () => postsApi.getBySlug(slug!),
    enabled: !!slug,
  });

  const post = data?.data;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error || !post) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <h1 className="text-2xl font-bold text-dark-100 mb-4">Post not found</h1>
        <Link to={backPath} className="btn-secondary">
          <ArrowLeft className="mr-2 w-5 h-5" />
          {t('common.backTo')} {t(backLabelKey)}
        </Link>
      </div>
    );
  }

  return (
    <>
      <Helmet>
        <title>{post.title} | Portfolio</title>
        <meta name="description" content={post.excerpt} />
      </Helmet>

      <article className="py-20">
        <div className="container max-w-4xl">
          {/* Back Link */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="mb-8"
          >
            <Link
              to={backPath}
              className="inline-flex items-center text-dark-400 hover:text-primary-400 transition-colors"
            >
              <ArrowLeft className="mr-2 w-5 h-5" />
              {t('common.backTo')} {t(backLabelKey)}
            </Link>
          </motion.div>

          {/* Hashtags */}
          {post.hashtags && post.hashtags.length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-6"
            >
              <HashtagList hashtags={post.hashtags} />
            </motion.div>
          )}

          {/* Header */}
          <motion.header
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="mb-8"
          >
            <h1 className="text-4xl md:text-5xl font-display font-bold text-dark-100">
              {post.title}
            </h1>
            {(post.githubUrl || post.liveUrl) && (
              <div className="flex gap-3 mt-4">
                {post.githubUrl && (
                  <a
                    href={post.githubUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-2 px-4 py-2 bg-dark-800 hover:bg-dark-700 border border-dark-600 rounded-lg text-dark-200 hover:text-primary-400 transition-colors text-sm"
                  >
                    <Github className="w-4 h-4" />
                    GitHub
                  </a>
                )}
                {post.liveUrl && (
                  <a
                    href={post.liveUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-2 px-4 py-2 bg-dark-800 hover:bg-dark-700 border border-dark-600 rounded-lg text-dark-200 hover:text-accent-teal transition-colors text-sm"
                  >
                    <ExternalLink className="w-4 h-4" />
                    Live Demo
                  </a>
                )}
              </div>
            )}
          </motion.header>

          {/* Media Carousel */}
          {post.media && post.media.length > 0 ? (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="mb-8"
            >
              <MediaCarousel media={post.media} />
            </motion.div>
          ) : post.featuredImage ? (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="rounded-xl overflow-hidden bg-dark-700 mb-8 max-w-xs mx-auto"
              style={{ aspectRatio: '4/5', maxHeight: '50vh' }}
            >
              <img
                src={getMediaUrl(post.featuredImage)}
                alt={post.title}
                className="w-full h-full object-cover"
              />
            </motion.div>
          ) : null}

          {/* Excerpt */}
          {post.excerpt && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
              className="mb-8"
            >
              <p className="text-xl text-dark-300 leading-relaxed">{post.excerpt}</p>
            </motion.div>
          )}

          {/* Rich Content */}
          {post.content && Object.keys(post.content).length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
              className="mb-8"
            >
              <RichTextContent content={post.content} />
            </motion.div>
          )}

          {/* Case Study Section */}
          {post.hasCaseStudy && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5 }}
              className="mt-12"
            >
              {/* Expandable Header */}
              <button
                onClick={() => setCaseStudyExpanded(!caseStudyExpanded)}
                className="w-full flex items-center justify-between p-4 bg-gradient-to-r from-primary-500/10 to-accent-teal/10 border border-primary-500/20 rounded-xl hover:border-primary-500/40 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-primary-500/20 flex items-center justify-center">
                    <TrendingUp className="w-5 h-5 text-primary-400" />
                  </div>
                  <span className="text-lg font-semibold text-dark-100">
                    {t('caseStudy.title')}
                  </span>
                </div>
                {caseStudyExpanded ? (
                  <ChevronUp className="w-5 h-5 text-dark-400" />
                ) : (
                  <ChevronDown className="w-5 h-5 text-dark-400" />
                )}
              </button>

              {/* Expandable Content */}
              <AnimatePresence>
                {caseStudyExpanded && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    transition={{ duration: 0.3 }}
                    className="overflow-hidden"
                  >
                    <div className="pt-6 space-y-6">
                      {/* Challenge */}
                      {post.caseStudyChallenge && (
                        <div className="card border-l-4 border-red-500/50">
                          <div className="flex items-start gap-4">
                            <div className="w-10 h-10 rounded-lg bg-red-500/20 flex items-center justify-center flex-shrink-0">
                              <Target className="w-5 h-5 text-red-400" />
                            </div>
                            <div>
                              <h3 className="text-lg font-semibold text-dark-100 mb-2">
                                {t('caseStudy.challenge')}
                              </h3>
                              <p className="text-dark-300 whitespace-pre-line">
                                {post.caseStudyChallenge}
                              </p>
                            </div>
                          </div>
                        </div>
                      )}

                      {/* Solution */}
                      {post.caseStudySolution && (
                        <div className="card border-l-4 border-blue-500/50">
                          <div className="flex items-start gap-4">
                            <div className="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center flex-shrink-0">
                              <Lightbulb className="w-5 h-5 text-blue-400" />
                            </div>
                            <div>
                              <h3 className="text-lg font-semibold text-dark-100 mb-2">
                                {t('caseStudy.solution')}
                              </h3>
                              <p className="text-dark-300 whitespace-pre-line">
                                {post.caseStudySolution}
                              </p>
                            </div>
                          </div>
                        </div>
                      )}

                      {/* Results */}
                      {post.caseStudyResults && (
                        <div className="card border-l-4 border-green-500/50">
                          <div className="flex items-start gap-4">
                            <div className="w-10 h-10 rounded-lg bg-green-500/20 flex items-center justify-center flex-shrink-0">
                              <TrendingUp className="w-5 h-5 text-green-400" />
                            </div>
                            <div>
                              <h3 className="text-lg font-semibold text-dark-100 mb-2">
                                {t('caseStudy.results')}
                              </h3>
                              <p className="text-dark-300 whitespace-pre-line">
                                {post.caseStudyResults}
                              </p>
                            </div>
                          </div>
                        </div>
                      )}

                      {/* Testimonial */}
                      {post.caseStudyTestimonial && (
                        <div className="card bg-gradient-to-br from-primary-500/5 to-accent-teal/5 border border-primary-500/20">
                          <div className="flex items-start gap-4">
                            <div className="w-10 h-10 rounded-lg bg-primary-500/20 flex items-center justify-center flex-shrink-0">
                              <Quote className="w-5 h-5 text-primary-400" />
                            </div>
                            <div>
                              <h3 className="text-lg font-semibold text-dark-100 mb-2">
                                {t('caseStudy.testimonial')}
                              </h3>
                              <blockquote className="text-dark-300 italic whitespace-pre-line">
                                "{post.caseStudyTestimonial}"
                              </blockquote>
                              {post.caseStudyTestimonialAuthor && (
                                <p className="text-primary-400 mt-3 font-medium">
                                  — {post.caseStudyTestimonialAuthor}
                                </p>
                              )}
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          )}
        </div>
      </article>
    </>
  );
}
