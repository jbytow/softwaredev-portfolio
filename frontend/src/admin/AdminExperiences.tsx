import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Plus, Edit2, Trash2, GripVertical, Building2, Calendar } from 'lucide-react';
import { adminExperiencesApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminExperiences() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.experiences(),
    queryFn: () => adminExperiencesApi.getAll(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminExperiencesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.experiences() });
      queryClient.invalidateQueries({ queryKey: ['experiences'] });
      toast.success('Experience deleted successfully');
      setDeleteId(null);
    },
    onError: () => {
      toast.error('Failed to delete experience');
    },
  });

  const experiences = data?.data || [];

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display font-bold text-dark-100">
            {t('admin.experiences.title')}
          </h1>
          <p className="text-dark-400 mt-1">{t('admin.experiences.subtitle')}</p>
        </div>
        <Link to="/admin/experiences/new" className="btn-primary">
          <Plus className="w-5 h-5 mr-2" />
          {t('admin.experiences.newExperience')}
        </Link>
      </div>

      {/* Experiences List */}
      {experiences.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-dark-400 mb-4">{t('admin.experiences.noExperiencesYet')}</p>
          <Link to="/admin/experiences/new" className="btn-primary inline-flex">
            <Plus className="w-5 h-5 mr-2" />
            {t('admin.experiences.createFirst')}
          </Link>
        </div>
      ) : (
        <div className="card">
          <div className="divide-y divide-dark-700">
            {experiences.map((exp) => (
              <div
                key={exp.id}
                className="flex items-center justify-between py-4 px-4 hover:bg-dark-700/50 transition-colors"
              >
                <div className="flex items-center space-x-4">
                  <GripVertical className="w-5 h-5 text-dark-500 cursor-grab" />
                  <div>
                    <h3 className="font-medium text-dark-100">{exp.titleEn}</h3>
                    <div className="flex items-center text-sm text-dark-400 mt-1 space-x-4">
                      <span className="flex items-center">
                        <Building2 className="w-3 h-3 mr-1" />
                        {exp.company}
                      </span>
                      <span className="flex items-center">
                        <Calendar className="w-3 h-3 mr-1" />
                        {exp.startDate} - {exp.endDate || 'Present'}
                      </span>
                    </div>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <Link
                    to={`/admin/experiences/${exp.id}/edit`}
                    className="p-2 text-dark-400 hover:text-primary-400 hover:bg-dark-600 rounded-lg transition-colors"
                  >
                    <Edit2 className="w-4 h-4" />
                  </Link>
                  <button
                    onClick={() => setDeleteId(exp.id)}
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
              Are you sure you want to delete this experience? This action cannot be undone.
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
