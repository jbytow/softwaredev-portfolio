import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { I18nextProvider, useTranslation } from 'react-i18next';
import { ArrowLeft } from 'lucide-react';
import type { AxiosError } from 'axios';
import { adminPostsApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import type { Post } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import PostArticle from '@/components/PostArticle';
import LoadError from '@/admin/components/LoadError';

type PreviewLanguage = 'en' | 'pl';

interface PreviewToolbarProps {
  published: boolean;
  language: PreviewLanguage;
  onLanguageChange: (language: PreviewLanguage) => void;
}

function PreviewToolbar({ published, language, onLanguageChange }: PreviewToolbarProps) {
  const { t } = useTranslation();

  return (
    <div className="card flex flex-wrap items-center justify-between gap-4">
      <p className="text-dark-100 font-medium">
        {published ? t('admin.preview.publishedBanner') : t('admin.preview.draftBanner')}
      </p>
      <div role="group" aria-label={t('admin.preview.language')} className="flex gap-1">
        {(['en', 'pl'] as const).map((code) => (
          <button
            key={code}
            type="button"
            aria-pressed={language === code}
            onClick={() => onLanguageChange(code)}
            className={language === code ? 'btn-primary' : 'btn-secondary'}
          >
            {code.toUpperCase()}
          </button>
        ))}
      </div>
    </div>
  );
}

// The admin DTO carries both languages; the public page gets one language and no fallback to the other.
function resolvePost(post: Post, language: PreviewLanguage): Post {
  const pick = <T,>(en: T, pl: T): T => (language === 'pl' ? pl : en);
  const challenge = pick(post.caseStudyChallengeEn, post.caseStudyChallengePl);
  const solution = pick(post.caseStudySolutionEn, post.caseStudySolutionPl);
  const results = pick(post.caseStudyResultsEn, post.caseStudyResultsPl);
  const testimonial = pick(post.caseStudyTestimonialEn, post.caseStudyTestimonialPl);

  return {
    ...post,
    title: pick(post.titleEn, post.titlePl) ?? '',
    excerpt: pick(post.excerptEn, post.excerptPl) ?? '',
    content: pick(post.contentEn, post.contentPl) ?? {},
    caseStudyChallenge: challenge,
    caseStudySolution: solution,
    caseStudyResults: results,
    caseStudyTestimonial: testimonial,
    hasCaseStudy: [challenge, solution, results, testimonial].some((text) => Boolean(text?.trim())),
  };
}

export default function AdminPostPreview() {
  const { id } = useParams<{ id: string }>();
  const { t, i18n } = useTranslation();
  const [language, setLanguage] = useState<PreviewLanguage>(i18n.language.startsWith('pl') ? 'pl' : 'en');
  // The article translates its own labels (case study headings), so they follow the previewed language
  const previewI18n = useMemo(() => i18n.cloneInstance({ lng: language }), [i18n, language]);

  const { data, isPending, isError, error, refetch } = useQuery({
    queryKey: queryKeys.admin.post(id!),
    queryFn: () => adminPostsApi.getById(id!),
    enabled: !!id,
  });

  const post = data?.data;

  if (isPending) {
    return <LoadingSpinner />;
  }

  const isNotFound = (error as AxiosError | null)?.response?.status === 404 || (!isError && !post);

  if (isError && !isNotFound) {
    return <LoadError onRetry={() => refetch()} />;
  }

  if (isNotFound || !post) {
    return (
      <div className="card text-center py-12">
        <h1 className="text-2xl font-bold text-dark-100 mb-4">{t('post.notFound')}</h1>
        <Link to="/admin/posts" className="btn-secondary">
          <ArrowLeft className="mr-2 w-5 h-5" />
          {t('admin.posts.title')}
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <PreviewToolbar published={post.published} language={language} onLanguageChange={setLanguage} />
      <I18nextProvider i18n={previewI18n}>
        <PostArticle
          post={resolvePost(post, language)}
          backPath={`/admin/posts/${post.id}/edit`}
          backLabel={t('admin.preview.backToEditor')}
        />
      </I18nextProvider>
    </div>
  );
}
