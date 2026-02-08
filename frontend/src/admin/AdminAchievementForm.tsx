import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ArrowLeft, Save } from 'lucide-react';
import { adminAchievementsApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import { AchievementCreateRequest } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminAchievementForm() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditing = Boolean(id);

  const [formData, setFormData] = useState<AchievementCreateRequest>({
    titleEn: '',
    titlePl: '',
    descriptionEn: '',
    descriptionPl: '',
    icon: '',
    year: '',
  });

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.achievement(id!),
    queryFn: () => adminAchievementsApi.getById(id!),
    enabled: isEditing,
  });

  useEffect(() => {
    if (data?.data) {
      const achievement = data.data;
      setFormData({
        titleEn: achievement.titleEn,
        titlePl: achievement.titlePl,
        descriptionEn: achievement.descriptionEn || '',
        descriptionPl: achievement.descriptionPl || '',
        icon: achievement.icon || '',
        year: achievement.year || '',
      });
    }
  }, [data]);

  const createMutation = useMutation({
    mutationFn: (data: AchievementCreateRequest) => adminAchievementsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.achievements() });
      toast.success('Achievement created');
      navigate('/admin/achievements');
    },
    onError: () => {
      toast.error('Failed to create achievement');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: AchievementCreateRequest) => adminAchievementsApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.achievements() });
      toast.success('Achievement updated');
      navigate('/admin/achievements');
    },
    onError: () => {
      toast.error('Failed to update achievement');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isEditing) {
      updateMutation.mutate(formData);
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/admin/achievements')}
          className="p-2 text-dark-400 hover:text-dark-100 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {isEditing ? t('admin.achievements.editAchievement') : t('admin.achievements.newAchievement')}
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Titles */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Titles</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Title (English) *</label>
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
              <label className="label">Title (Polish) *</label>
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
        </div>

        {/* Descriptions */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Descriptions</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Description (English)</label>
              <textarea
                name="descriptionEn"
                value={formData.descriptionEn}
                onChange={handleChange}
                className="input"
                rows={3}
              />
            </div>
            <div>
              <label className="label">Description (Polish)</label>
              <textarea
                name="descriptionPl"
                value={formData.descriptionPl}
                onChange={handleChange}
                className="input"
                rows={3}
              />
            </div>
          </div>
        </div>

        {/* Icon & Year */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Icon & Year</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Icon Name</label>
              <input
                type="text"
                name="icon"
                value={formData.icon}
                onChange={handleChange}
                className="input"
                placeholder="e.g., zap, rocket, award, trophy"
              />
              <p className="text-sm text-dark-500 mt-2">
                zap, rocket, award, database, sparkles, trophy, star, code, terminal
              </p>
            </div>
            <div>
              <label className="label">Year</label>
              <input
                type="text"
                name="year"
                value={formData.year}
                onChange={handleChange}
                className="input"
                placeholder="e.g., 2024"
              />
            </div>
          </div>
        </div>

        <div className="flex justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate('/admin/achievements')}
            className="btn-secondary"
          >
            {t('common.cancel')}
          </button>
          <button type="submit" disabled={isPending} className="btn-primary">
            <Save className="w-5 h-5 mr-2" />
            {t('common.save')}
          </button>
        </div>
      </form>
    </div>
  );
}
