import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { FileText, Image, Eye, PenLine, Plus } from 'lucide-react';
import { adminPostsApi, adminMediaApi } from '@/services/api';
import { useAuth } from '@/contexts/AuthContext';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminDashboard() {
  const { t } = useTranslation();
  const { user } = useAuth();

  const { data: postsData, isLoading: postsLoading } = useQuery({
    queryKey: queryKeys.admin.posts(),
    queryFn: () => adminPostsApi.getAll(),
  });

  const { data: mediaData, isLoading: mediaLoading } = useQuery({
    queryKey: queryKeys.admin.media(),
    queryFn: () => adminMediaApi.getAll(0, 1),
  });

  const posts = postsData?.data || [];
  const totalMedia = mediaData?.data?.totalElements || 0;
  const publishedPosts = posts.filter((p) => p.published).length;
  const draftPosts = posts.length - publishedPosts;

  if (postsLoading || mediaLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {t('admin.dashboard.title')}
        </h1>
        <p className="text-dark-400 mt-1">
          {t('admin.dashboard.welcome')}, {user?.name}!
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div className="w-12 h-12 rounded-xl bg-primary-500/20 flex items-center justify-center">
              <FileText className="w-6 h-6 text-primary-400" />
            </div>
          </div>
          <div className="text-3xl font-bold text-dark-100">{posts.length}</div>
          <div className="text-dark-500 text-sm">{t('admin.dashboard.totalPosts')}</div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div className="w-12 h-12 rounded-xl bg-green-500/20 flex items-center justify-center">
              <Eye className="w-6 h-6 text-green-400" />
            </div>
          </div>
          <div className="text-3xl font-bold text-dark-100">{publishedPosts}</div>
          <div className="text-dark-500 text-sm">{t('admin.dashboard.publishedPosts')}</div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div className="w-12 h-12 rounded-xl bg-yellow-500/20 flex items-center justify-center">
              <PenLine className="w-6 h-6 text-yellow-400" />
            </div>
          </div>
          <div className="text-3xl font-bold text-dark-100">{draftPosts}</div>
          <div className="text-dark-500 text-sm">{t('admin.dashboard.draftPosts')}</div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div className="w-12 h-12 rounded-xl bg-accent/20 flex items-center justify-center">
              <Image className="w-6 h-6 text-accent" />
            </div>
          </div>
          <div className="text-3xl font-bold text-dark-100">{totalMedia}</div>
          <div className="text-dark-500 text-sm">{t('admin.dashboard.totalMedia')}</div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid md:grid-cols-2 gap-6">
        <div className="card">
          <h2 className="text-xl font-semibold text-dark-100 mb-4">Quick Actions</h2>
          <div className="space-y-3">
            <Link
              to="/admin/posts/new"
              className="flex items-center p-4 bg-dark-700 hover:bg-dark-600 rounded-lg transition-colors"
            >
              <Plus className="w-5 h-5 text-primary-400 mr-3" />
              <span className="text-dark-200">{t('admin.posts.newPost')}</span>
            </Link>
            <Link
              to="/admin/media"
              className="flex items-center p-4 bg-dark-700 hover:bg-dark-600 rounded-lg transition-colors"
            >
              <Image className="w-5 h-5 text-primary-400 mr-3" />
              <span className="text-dark-200">{t('admin.media.upload')}</span>
            </Link>
          </div>
        </div>

        <div className="card">
          <h2 className="text-xl font-semibold text-dark-100 mb-4">
            {t('admin.dashboard.recentActivity')}
          </h2>
          <div className="space-y-3">
            {posts.slice(0, 5).map((post) => (
              <Link
                key={post.id}
                to={`/admin/posts/${post.id}/edit`}
                className="flex items-center justify-between p-3 bg-dark-700 hover:bg-dark-600 rounded-lg transition-colors"
              >
                <span className="text-dark-200 truncate">{post.title}</span>
                <span
                  className={`text-xs px-2 py-1 rounded ${
                    post.published
                      ? 'bg-green-500/20 text-green-400'
                      : 'bg-yellow-500/20 text-yellow-400'
                  }`}
                >
                  {post.published ? t('common.published') : t('common.draft')}
                </span>
              </Link>
            ))}
            {posts.length === 0 && (
              <p className="text-dark-500 text-center py-4">
                {t('admin.posts.noPostsYet')}
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
