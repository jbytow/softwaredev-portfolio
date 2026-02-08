import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  Plus,
  Search,
  Edit,
  Trash2,
  Eye,
  EyeOff,
  GripVertical,
} from 'lucide-react';
import { clsx } from 'clsx';
import { adminPostsApi } from '@/services/api';
import { Category, Post } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import { getMediaUrl } from '@/lib/mediaUrl';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminPosts() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<Category | ''>('');

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.posts(selectedCategory || undefined),
    queryFn: () => adminPostsApi.getAll(selectedCategory || undefined),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminPostsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'posts'] });
      toast.success('Post deleted successfully');
    },
    onError: () => {
      toast.error('Failed to delete post');
    },
  });

  const togglePublishMutation = useMutation({
    mutationFn: (id: string) => adminPostsApi.togglePublish(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'posts'] });
      toast.success('Post status updated');
    },
    onError: () => {
      toast.error('Failed to update post status');
    },
  });

  const posts = data?.data || [];
  const filteredPosts = posts.filter((post) =>
    post.title.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleDelete = (post: Post) => {
    if (window.confirm(`Are you sure you want to delete "${post.title}"?`)) {
      deleteMutation.mutate(post.id);
    }
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {t('admin.posts.title')}
        </h1>
        <Link to="/admin/posts/new" className="btn-primary">
          <Plus className="w-5 h-5 mr-2" />
          {t('admin.posts.newPost')}
        </Link>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-dark-500" />
          <input
            type="text"
            placeholder={t('common.search')}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input pl-10"
          />
        </div>
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value as Category | '')}
          className="input w-full sm:w-48"
        >
          <option value="">{t('common.all')}</option>
          {Object.values(Category).map((cat) => (
            <option key={cat} value={cat}>
              {t(`categories.${cat}`)}
            </option>
          ))}
        </select>
      </div>

      {/* Posts Table */}
      <div className="card overflow-hidden p-0">
        {filteredPosts.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-dark-400">{t('admin.posts.noPostsYet')}</p>
            <Link to="/admin/posts/new" className="btn-primary mt-4 inline-flex">
              <Plus className="w-5 h-5 mr-2" />
              {t('admin.posts.createFirst')}
            </Link>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-dark-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-dark-400 uppercase tracking-wider">
                    <GripVertical className="w-4 h-4" />
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-dark-400 uppercase tracking-wider">
                    Title
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-dark-400 uppercase tracking-wider">
                    Category
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-dark-400 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-dark-400 uppercase tracking-wider">
                    {t('common.actions')}
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-dark-700">
                {filteredPosts.map((post) => (
                  <tr key={post.id} className="hover:bg-dark-700/50">
                    <td className="px-6 py-4">
                      <GripVertical className="w-5 h-5 text-dark-500 cursor-grab" />
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        {post.featuredImage && (
                          <img
                            src={getMediaUrl(post.featuredImage)}
                            alt=""
                            className="w-10 h-10 rounded object-cover mr-3"
                          />
                        )}
                        <div>
                          <div className="text-dark-100 font-medium">{post.title}</div>
                          <div className="text-dark-500 text-sm">{post.slug}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="badge-primary">{post.categoryLabel}</span>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={clsx(
                          'inline-flex items-center px-2 py-1 rounded text-xs font-medium',
                          post.published
                            ? 'bg-green-500/20 text-green-400'
                            : 'bg-yellow-500/20 text-yellow-400'
                        )}
                      >
                        {post.published ? (
                          <>
                            <Eye className="w-3 h-3 mr-1" />
                            {t('common.published')}
                          </>
                        ) : (
                          <>
                            <EyeOff className="w-3 h-3 mr-1" />
                            {t('common.draft')}
                          </>
                        )}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          onClick={() => togglePublishMutation.mutate(post.id)}
                          className="p-2 text-dark-400 hover:text-primary-400 hover:bg-dark-700 rounded-lg transition-colors"
                          title={post.published ? 'Unpublish' : 'Publish'}
                        >
                          {post.published ? (
                            <EyeOff className="w-5 h-5" />
                          ) : (
                            <Eye className="w-5 h-5" />
                          )}
                        </button>
                        <Link
                          to={`/admin/posts/${post.id}/edit`}
                          className="p-2 text-dark-400 hover:text-primary-400 hover:bg-dark-700 rounded-lg transition-colors"
                        >
                          <Edit className="w-5 h-5" />
                        </Link>
                        <button
                          onClick={() => handleDelete(post)}
                          className="p-2 text-dark-400 hover:text-red-400 hover:bg-dark-700 rounded-lg transition-colors"
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
