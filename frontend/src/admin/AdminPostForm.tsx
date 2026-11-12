import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ArrowLeft, Save, Eye, EyeOff, ExternalLink, X, Plus, ChevronDown, ChevronUp } from 'lucide-react';
import { adminPostsApi } from '@/services/api';
import { Category, PostCreateRequest, PostUpdateRequest, Media } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';
import LoadError from '@/admin/components/LoadError';
import { useUnsavedChanges } from './hooks/useUnsavedChanges';
import MediaManager from '@/admin/components/MediaManager';
import RichTextEditor from '@/components/RichTextEditor';
import { getApiErrorMessage } from '@/lib/apiError';

// Mirrors the backend rule: a case study exists when the challenge, solution, results or testimonial
// has text. The form edits both languages, so text in either one counts.
type CaseStudyField = 'caseStudyChallenge' | 'caseStudySolution' | 'caseStudyResults' | 'caseStudyTestimonial';

function hasAnyCaseStudyText(
  post: Partial<Record<`${CaseStudyField}${'En' | 'Pl'}`, string | null>>
): boolean {
  return [
    post.caseStudyChallengeEn,
    post.caseStudySolutionEn,
    post.caseStudyResultsEn,
    post.caseStudyTestimonialEn,
    post.caseStudyChallengePl,
    post.caseStudySolutionPl,
    post.caseStudyResultsPl,
    post.caseStudyTestimonialPl,
  ].some((text) => Boolean(text?.trim()));
}

export default function AdminPostForm() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditing = !!id;

  const [formData, setFormData] = useState<PostCreateRequest>({
    category: Category.PERSONAL_PROJECT,
    titleEn: '',
    titlePl: '',
    slug: '',
    excerptEn: '',
    excerptPl: '',
    contentEn: {},
    contentPl: {},
    featuredImage: '',
    githubUrl: '',
    liveUrl: '',
    published: false,
    hashtags: [],
    caseStudyChallengeEn: '',
    caseStudyChallengePl: '',
    caseStudySolutionEn: '',
    caseStudySolutionPl: '',
    caseStudyResultsEn: '',
    caseStudyResultsPl: '',
    caseStudyTestimonialEn: '',
    caseStudyTestimonialPl: '',
    caseStudyTestimonialAuthor: '',
  });
  const [baseline, setBaseline] = useState(() => JSON.stringify(formData));
  const { confirmLeave } = useUnsavedChanges(JSON.stringify(formData) !== baseline);

  const [media, setMedia] = useState<Media[]>([]);
  const [newHashtag, setNewHashtag] = useState('');
  const [caseStudyExpanded, setCaseStudyExpanded] = useState(false);

  // Post id the form was seeded for. Later refetches (window focus, reconnect, invalidation
  // after a media operation) must not overwrite unsaved edits.
  const [seededId, setSeededId] = useState<string | null>(null);

  const {
    data: postData,
    isLoading: postLoading,
    isFetching: postFetching,
    isError: loadFailed,
    refetch,
  } = useQuery({
    queryKey: queryKeys.admin.post(id!),
    queryFn: () => adminPostsApi.getById(id!),
    enabled: isEditing,
  });

  // Wait for the fetch to finish: cached data may be stale and about to be replaced.
  useEffect(() => {
    if (postData?.data && !postFetching && seededId !== id) {
      const post = postData.data;
      const seeded: typeof formData = {
        category: post.category,
        titleEn: post.titleEn || '',
        titlePl: post.titlePl || '',
        slug: post.slug,
        excerptEn: post.excerptEn || '',
        excerptPl: post.excerptPl || '',
        contentEn: post.contentEn || {},
        contentPl: post.contentPl || {},
        featuredImage: post.featuredImage || '',
        githubUrl: post.githubUrl || '',
        liveUrl: post.liveUrl || '',
        published: post.published,
        hashtags: post.hashtags || [],
        caseStudyChallengeEn: post.caseStudyChallengeEn || '',
        caseStudyChallengePl: post.caseStudyChallengePl || '',
        caseStudySolutionEn: post.caseStudySolutionEn || '',
        caseStudySolutionPl: post.caseStudySolutionPl || '',
        caseStudyResultsEn: post.caseStudyResultsEn || '',
        caseStudyResultsPl: post.caseStudyResultsPl || '',
        caseStudyTestimonialEn: post.caseStudyTestimonialEn || '',
        caseStudyTestimonialPl: post.caseStudyTestimonialPl || '',
        caseStudyTestimonialAuthor: post.caseStudyTestimonialAuthor || '',
      };
      setFormData(seeded);
      setBaseline(JSON.stringify(seeded));
      setMedia(post.media || []);
      setSeededId(id ?? null);
      // Auto-expand if there's existing case study content in either language
      if (hasAnyCaseStudyText(post)) {
        setCaseStudyExpanded(true);
      }
    }
  }, [postData, postFetching, seededId, id]);

  const createMutation = useMutation({
    mutationFn: (data: PostCreateRequest) => adminPostsApi.create(data),
    onSuccess: () => {
      setBaseline(JSON.stringify(formData));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.postsAll() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.posts() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.hashtags() });
      toast.success(t('admin.posts.created'));
      navigate('/admin/posts');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.posts.createFailed')));
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: PostUpdateRequest) => adminPostsApi.update(id!, data),
    onSuccess: () => {
      setBaseline(JSON.stringify(formData));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.postsAll() });
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.post(id!) });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.posts() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.post() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.hashtags() });
      toast.success(t('admin.posts.updated'));
      navigate('/admin/posts');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.posts.updateFailed')));
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // A blank slug is omitted so update keeps the stored one and create lets the backend generate it
    const submitData = { ...formData, slug: formData.slug?.trim() || undefined };
    if (isEditing) {
      updateMutation.mutate(submitData);
    } else {
      createMutation.mutate(submitData);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
    }));
  };

  const addHashtag = () => {
    const tag = newHashtag.trim().toLowerCase().replace(/[^a-z0-9]/g, '');
    if (tag && !formData.hashtags?.includes(tag)) {
      setFormData((prev) => ({
        ...prev,
        hashtags: [...(prev.hashtags || []), tag],
      }));
    }
    setNewHashtag('');
  };

  const removeHashtag = (tagToRemove: string) => {
    setFormData((prev) => ({
      ...prev,
      hashtags: (prev.hashtags || []).filter((tag) => tag !== tagToRemove),
    }));
  };

  const handleHashtagKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addHashtag();
    }
  };

  const hasCaseStudyContent = hasAnyCaseStudyText(formData);

  if (postLoading || (isEditing && postData?.data && seededId !== id)) {
    return <LoadingSpinner />;
  }

  if (loadFailed && !postData) {
    return <LoadError onRetry={() => refetch()} />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => confirmLeave() && navigate('/admin/posts')}
            className="p-2 text-dark-400 hover:text-dark-100 hover:bg-dark-700 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-3xl font-display font-bold text-dark-100">
            {isEditing ? t('admin.posts.editPost') : t('admin.posts.newPost')}
          </h1>
        </div>
        <div className="flex items-center space-x-3">
          {isEditing && (
            // A new tab keeps this form and its unsaved edits; the preview shows the last saved state
            <Link
              to={`/admin/posts/${id}/preview`}
              target="_blank"
              rel="noopener noreferrer"
              className="btn-secondary"
            >
              <ExternalLink className="w-5 h-5 mr-2" />
              {t('admin.preview.button')}
            </Link>
          )}
          <button
            type="button"
            onClick={() => setFormData((prev) => ({ ...prev, published: !prev.published }))}
            className={`btn-secondary ${formData.published ? 'bg-green-500/20 text-green-400' : ''}`}
          >
            {formData.published ? (
              <>
                <Eye className="w-5 h-5 mr-2" />
                {t('common.published')}
              </>
            ) : (
              <>
                <EyeOff className="w-5 h-5 mr-2" />
                {t('common.draft')}
              </>
            )}
          </button>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Category */}
        <div className="card">
          <label className="label">{t('admin.common.category')}</label>
          <select
            name="category"
            value={formData.category}
            onChange={handleChange}
            className="input"
            required
          >
            {Object.values(Category).map((cat) => (
              <option key={cat} value={cat}>
                {t(`categories.${cat}`)}
              </option>
            ))}
          </select>
        </div>

        {/* Titles */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">{t('admin.posts.titles')}</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.posts.titleEn')}</label>
              <input
                type="text"
                name="titleEn"
                value={formData.titleEn}
                onChange={handleChange}
                className="input"
                required
              />
            </div>
            <div>
              <label className="label">{t('admin.posts.titlePl')}</label>
              <input
                type="text"
                name="titlePl"
                value={formData.titlePl}
                onChange={handleChange}
                className="input"
                required
              />
            </div>
          </div>
          <div>
            <label className="label">{t('admin.posts.slug')}</label>
            <input
              type="text"
              name="slug"
              value={formData.slug}
              onChange={handleChange}
              className="input"
              placeholder={t('admin.posts.slugPlaceholder')}
            />
          </div>
        </div>

        {/* Hashtags */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">{t('admin.posts.hashtags')}</h3>
          <div className="flex gap-2">
            <input
              type="text"
              value={newHashtag}
              onChange={(e) => setNewHashtag(e.target.value)}
              onKeyDown={handleHashtagKeyDown}
              placeholder={t('admin.posts.addHashtag')}
              className="input flex-1"
            />
            <button
              type="button"
              onClick={addHashtag}
              className="btn-secondary"
            >
              <Plus className="w-5 h-5" />
            </button>
          </div>
          {formData.hashtags && formData.hashtags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {formData.hashtags.map((tag) => (
                <span
                  key={tag}
                  className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-primary-500/20 text-primary-400"
                >
                  #{tag}
                  <button
                    type="button"
                    onClick={() => removeHashtag(tag)}
                    className="hover:text-primary-300"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>

        {/* Excerpts */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">{t('admin.posts.excerpts')}</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.posts.excerptEn')}</label>
              <textarea
                name="excerptEn"
                value={formData.excerptEn}
                onChange={handleChange}
                className="input"
                rows={3}
              />
            </div>
            <div>
              <label className="label">{t('admin.posts.excerptPl')}</label>
              <textarea
                name="excerptPl"
                value={formData.excerptPl}
                onChange={handleChange}
                className="input"
                rows={3}
              />
            </div>
          </div>
        </div>

        {/* Content */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">{t('admin.posts.content')}</h3>
          <div className="space-y-4">
            <div>
              <label className="label">{t('admin.posts.contentEn')}</label>
              <RichTextEditor
                content={formData.contentEn || {}}
                onChange={(content) => setFormData((prev) => ({ ...prev, contentEn: content }))}
                placeholder={t('admin.posts.contentEnPlaceholder')}
              />
            </div>
            <div>
              <label className="label">{t('admin.posts.contentPl')}</label>
              <RichTextEditor
                content={formData.contentPl || {}}
                onChange={(content) => setFormData((prev) => ({ ...prev, contentPl: content }))}
                placeholder={t('admin.posts.contentPlPlaceholder')}
              />
            </div>
          </div>
        </div>

        {/* Featured Image */}
        <div className="card">
          <label className="label">{t('admin.posts.featuredImageUrl')}</label>
          <input
            type="text"
            name="featuredImage"
            value={formData.featuredImage}
            onChange={handleChange}
            className="input"
            placeholder="https://..."
          />
          {formData.featuredImage && (
            <img
              src={formData.featuredImage}
              alt={t('admin.preview.button')}
              className="mt-4 max-h-48 rounded-lg object-cover"
            />
          )}
        </div>

        {/* GitHub & Live URL */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">{t('admin.posts.projectLinks')}</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.posts.githubUrl')}</label>
              <input
                type="url"
                name="githubUrl"
                value={formData.githubUrl}
                onChange={handleChange}
                className="input"
                placeholder="https://github.com/..."
              />
            </div>
            <div>
              <label className="label">{t('admin.posts.liveUrl')}</label>
              <input
                type="url"
                name="liveUrl"
                value={formData.liveUrl}
                onChange={handleChange}
                className="input"
                placeholder="https://..."
              />
            </div>
          </div>
        </div>

        {/* Media Manager */}
        {isEditing && id ? (
          <div className="card space-y-4">
            <h3 className="text-lg font-semibold text-dark-100">{t('admin.posts.mediaGallery')}</h3>
            <p className="text-sm text-dark-400">
              {t('admin.posts.mediaGalleryHint')}
            </p>
            <MediaManager
              postId={id}
              media={media}
              onMediaChange={setMedia}
            />
          </div>
        ) : (
          <div className="card">
            <h3 className="text-lg font-semibold text-dark-100">{t('admin.posts.mediaGallery')}</h3>
            <p className="text-sm text-dark-400 mt-2">
              {t('admin.posts.mediaGalleryBeforeSave')}
            </p>
          </div>
        )}

        {/* Case Study Section */}
        <div className="card">
          <button
            type="button"
            onClick={() => setCaseStudyExpanded(!caseStudyExpanded)}
            className="w-full flex items-center justify-between"
          >
            <div className="flex items-center gap-3">
              <h3 className="text-lg font-semibold text-dark-100">
                {t('admin.posts.caseStudy.title')}
              </h3>
              {hasCaseStudyContent && (
                <span className="px-2 py-0.5 text-xs rounded-full bg-primary-500/20 text-primary-400">
                  {t('admin.posts.caseStudy.hasContent')}
                </span>
              )}
            </div>
            {caseStudyExpanded ? (
              <ChevronUp className="w-5 h-5 text-dark-400" />
            ) : (
              <ChevronDown className="w-5 h-5 text-dark-400" />
            )}
          </button>
          <p className="text-sm text-dark-400 mt-1">
            {t('admin.posts.caseStudy.description')}
          </p>

          {caseStudyExpanded && (
            <div className="mt-6 space-y-6 pt-6 border-t border-dark-700">
              {/* Challenge */}
              <div className="space-y-4">
                <h4 className="font-medium text-dark-200">{t('caseStudy.challenge')}</h4>
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <label className="label">{t('admin.posts.challengeEn')}</label>
                    <textarea
                      name="caseStudyChallengeEn"
                      value={formData.caseStudyChallengeEn}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.challengeEnPlaceholder')}
                    />
                  </div>
                  <div>
                    <label className="label">{t('admin.posts.challengePl')}</label>
                    <textarea
                      name="caseStudyChallengePl"
                      value={formData.caseStudyChallengePl}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.challengePlPlaceholder')}
                    />
                  </div>
                </div>
              </div>

              {/* Solution */}
              <div className="space-y-4">
                <h4 className="font-medium text-dark-200">{t('caseStudy.solution')}</h4>
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <label className="label">{t('admin.posts.solutionEn')}</label>
                    <textarea
                      name="caseStudySolutionEn"
                      value={formData.caseStudySolutionEn}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.solutionEnPlaceholder')}
                    />
                  </div>
                  <div>
                    <label className="label">{t('admin.posts.solutionPl')}</label>
                    <textarea
                      name="caseStudySolutionPl"
                      value={formData.caseStudySolutionPl}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.solutionPlPlaceholder')}
                    />
                  </div>
                </div>
              </div>

              {/* Results */}
              <div className="space-y-4">
                <h4 className="font-medium text-dark-200">{t('caseStudy.results')}</h4>
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <label className="label">{t('admin.posts.resultsEn')}</label>
                    <textarea
                      name="caseStudyResultsEn"
                      value={formData.caseStudyResultsEn}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.resultsEnPlaceholder')}
                    />
                  </div>
                  <div>
                    <label className="label">{t('admin.posts.resultsPl')}</label>
                    <textarea
                      name="caseStudyResultsPl"
                      value={formData.caseStudyResultsPl}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.resultsPlPlaceholder')}
                    />
                  </div>
                </div>
              </div>

              {/* Testimonial */}
              <div className="space-y-4">
                <h4 className="font-medium text-dark-200">{t('caseStudy.testimonial')} ({t('common.optional')})</h4>
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <label className="label">{t('admin.posts.testimonialEn')}</label>
                    <textarea
                      name="caseStudyTestimonialEn"
                      value={formData.caseStudyTestimonialEn}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.testimonialEnPlaceholder')}
                    />
                  </div>
                  <div>
                    <label className="label">{t('admin.posts.testimonialPl')}</label>
                    <textarea
                      name="caseStudyTestimonialPl"
                      value={formData.caseStudyTestimonialPl}
                      onChange={handleChange}
                      className="input"
                      rows={3}
                      placeholder={t('admin.posts.testimonialPlPlaceholder')}
                    />
                  </div>
                </div>
                <div>
                  <label className="label">{t('admin.posts.testimonialAuthor')}</label>
                  <input
                    type="text"
                    name="caseStudyTestimonialAuthor"
                    value={formData.caseStudyTestimonialAuthor}
                    onChange={handleChange}
                    className="input"
                    placeholder={t('admin.posts.testimonialAuthorPlaceholder')}
                  />
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Submit */}
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => confirmLeave() && navigate('/admin/posts')}
            className="btn-secondary"
          >
            {t('common.cancel')}
          </button>
          <button
            type="submit"
            disabled={createMutation.isPending || updateMutation.isPending}
            className="btn-primary disabled:opacity-50"
          >
            <Save className="w-5 h-5 mr-2" />
            {t('common.save')}
          </button>
        </div>
      </form>
    </div>
  );
}
