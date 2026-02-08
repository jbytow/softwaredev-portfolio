import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Plus, Edit2, Trash2, GripVertical } from 'lucide-react';
import { adminSoftSkillsApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminSoftSkills() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.softSkills(),
    queryFn: () => adminSoftSkillsApi.getAll(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminSoftSkillsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkills() });
      queryClient.invalidateQueries({ queryKey: ['softSkills'] });
      toast.success('Soft skill deleted successfully');
      setDeleteId(null);
    },
    onError: () => {
      toast.error('Failed to delete soft skill');
    },
  });

  const softSkills = data?.data || [];

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display font-bold text-dark-100">
            {t('admin.softSkills.title')}
          </h1>
          <p className="text-dark-400 mt-1">{t('admin.softSkills.subtitle')}</p>
        </div>
        <Link to="/admin/soft-skills/new" className="btn-primary">
          <Plus className="w-5 h-5 mr-2" />
          {t('admin.softSkills.newSkill')}
        </Link>
      </div>

      {/* Skills List */}
      {softSkills.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-dark-400 mb-4">{t('admin.softSkills.noSkillsYet')}</p>
          <Link to="/admin/soft-skills/new" className="btn-primary inline-flex">
            <Plus className="w-5 h-5 mr-2" />
            {t('admin.softSkills.createFirst')}
          </Link>
        </div>
      ) : (
        <div className="card">
          <div className="divide-y divide-dark-700">
            {softSkills.map((skill) => (
              <div
                key={skill.id}
                className="flex items-center justify-between py-4 px-4 hover:bg-dark-700/50 transition-colors"
              >
                <div className="flex items-center space-x-4">
                  <GripVertical className="w-5 h-5 text-dark-500 cursor-grab" />
                  <div>
                    <h3 className="font-medium text-dark-100">{skill.nameEn}</h3>
                    <p className="text-sm text-dark-400">{skill.namePl}</p>
                    {skill.icon && (
                      <span className="text-xs text-dark-500">Icon: {skill.icon}</span>
                    )}
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <Link
                    to={`/admin/soft-skills/${skill.id}/edit`}
                    className="p-2 text-dark-400 hover:text-primary-400 hover:bg-dark-600 rounded-lg transition-colors"
                  >
                    <Edit2 className="w-4 h-4" />
                  </Link>
                  <button
                    onClick={() => setDeleteId(skill.id)}
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
      {deleteId && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="card max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-dark-100 mb-2">
              {t('common.confirm')} Delete
            </h3>
            <p className="text-dark-400 mb-6">
              Are you sure you want to delete this soft skill? This action cannot be undone.
            </p>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setDeleteId(null)}
                className="btn-secondary"
              >
                {t('common.cancel')}
              </button>
              <button
                onClick={() => deleteMutation.mutate(deleteId)}
                disabled={deleteMutation.isPending}
                className="btn-primary bg-red-500 hover:bg-red-600 disabled:opacity-50"
              >
                {t('common.delete')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
