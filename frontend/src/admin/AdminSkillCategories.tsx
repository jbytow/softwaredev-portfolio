import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Plus, Edit2, Trash2, GripVertical } from 'lucide-react';
import { adminSkillCategoriesApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';
import { getApiErrorMessage } from '@/lib/apiError';
import ConfirmDeleteModal from '@/admin/components/ConfirmDeleteModal';

export default function AdminSkillCategories() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.skillCategories(),
    queryFn: () => adminSkillCategoriesApi.getAll(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminSkillCategoriesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.skillCategories() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.skillCategories() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.skills() });
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkills() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.softSkills() });
      toast.success(t('admin.skillCategories.deleted'));
      setDeleteId(null);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.skillCategories.deleteFailed')));
    },
  });

  const categories = data?.data || [];

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display font-bold text-dark-100">
            {t('admin.skillCategories.title')}
          </h1>
          <p className="text-dark-400 mt-1">{t('admin.skillCategories.subtitle')}</p>
        </div>
        <Link to="/admin/skill-categories/new" className="btn-primary">
          <Plus className="w-5 h-5 mr-2" />
          {t('admin.skillCategories.newCategory')}
        </Link>
      </div>

      {/* Categories List */}
      {categories.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-dark-400 mb-4">{t('admin.skillCategories.noCategoriesYet')}</p>
          <Link to="/admin/skill-categories/new" className="btn-primary inline-flex">
            <Plus className="w-5 h-5 mr-2" />
            {t('admin.skillCategories.createFirst')}
          </Link>
        </div>
      ) : (
        <div className="card">
          <div className="divide-y divide-dark-700">
            {categories.map((category) => (
              <div
                key={category.id}
                className="flex items-center justify-between py-4 px-4 hover:bg-dark-700/50 transition-colors"
              >
                <div className="flex items-center space-x-4">
                  <GripVertical className="w-5 h-5 text-dark-500 cursor-grab" />
                  <div>
                    <h3 className="font-medium text-dark-100">{category.nameEn}</h3>
                    <p className="text-sm text-dark-400">{category.namePl}</p>
                    <span className="text-xs text-dark-500">
                      {category.skillCount} {t('admin.skillCategories.skillCount')}
                    </span>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <Link
                    to={`/admin/skill-categories/${category.id}/edit`}
                    className="p-2 text-dark-400 hover:text-primary-400 hover:bg-dark-600 rounded-lg transition-colors"
                  >
                    <Edit2 className="w-4 h-4" />
                  </Link>
                  <button
                    onClick={() => setDeleteId(category.id)}
                    className="p-2 text-dark-400 hover:text-red-400 hover:bg-dark-600 rounded-lg transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      <ConfirmDeleteModal
        open={deleteId !== null}
        title={t('admin.common.confirmDelete')}
        message={t('admin.skillCategories.deleteConfirm')}
        isPending={deleteMutation.isPending}
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        onCancel={() => setDeleteId(null)}
      />
    </div>
  );
}
