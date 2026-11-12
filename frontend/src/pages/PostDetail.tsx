import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import type { AxiosError } from 'axios';
import { ArrowLeft } from 'lucide-react';
import { useNerdTranslation } from '@/hooks/useNerdTranslation';
import { postsApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';
import PostArticle from '@/components/PostArticle';

export default function PostDetail() {
  const { slug } = useParams<{ slug: string }>();
  const { language } = useLanguage();
  const { t } = useNerdTranslation();

  const backPath = '/projects';
  const backLabelKey = 'post.backToProjects';

  const { data, isPending, isError, error, refetch } = useQuery({
    queryKey: queryKeys.post(language, slug!),
    queryFn: () => postsApi.getBySlug(slug!),
    enabled: !!slug,
  });

  const post = data?.data;

  if (isPending) {
    return <LoadingSpinner fullScreen />;
  }

  // Only a 404 (or an empty body) is a missing page; 5xx and network errors are retryable
  const isNotFound = (error as AxiosError | null)?.response?.status === 404 || (!isError && !post);

  if (isError && !isNotFound) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <p className="text-dark-400 mb-4">{t('common.loadError')}</p>
        <button type="button" onClick={() => refetch()} className="btn-secondary">
          {t('common.retry')}
        </button>
      </div>
    );
  }

  if (isNotFound || !post) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center">
        <h1 className="text-2xl font-bold text-dark-100 mb-4">{t('post.notFound')}</h1>
        <Link to={backPath} className="btn-secondary">
          <ArrowLeft className="mr-2 w-5 h-5" />
          {t(backLabelKey)}
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

      <PostArticle post={post} backPath={backPath} backLabel={t(backLabelKey)} />
    </>
  );
}
