import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Plus, Edit2, Trash2, GripVertical, Building2, Calendar } from 'lucide-react';
import { adminExperiencesApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';
import { getApiErrorMessage } from '@/lib/apiError';
import ConfirmDeleteModal from '@/admin/components/ConfirmDeleteModal';

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
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.experiences() });
      toast.success(t('admin.experiences.deleted'));
      setDeleteId(null);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.experiences.deleteFailed')));
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
                        {exp.startDate} - {exp.endDate || t('admin.experiences.present')}
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
      <ConfirmDeleteModal
        open={deleteId !== null}
        title={t('admin.common.confirmDelete')}
        message={t('admin.experiences.confirmDeleteMessage')}
        isPending={deleteMutation.isPending}
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        onCancel={() => setDeleteId(null)}
      />
    </div>
  );
}
