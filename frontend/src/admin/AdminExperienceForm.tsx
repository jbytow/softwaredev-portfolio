import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ArrowLeft, Save, Plus, X } from 'lucide-react';
import { adminExperiencesApi } from '@/services/api';
import { ExperienceCreateRequest } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminExperienceForm() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditing = !!id;

  const [formData, setFormData] = useState<ExperienceCreateRequest>({
    titleEn: '',
    titlePl: '',
    company: '',
    startDate: '',
    endDate: '',
    descriptionEn: '',
    descriptionPl: '',
    achievementsEn: [],
    achievementsPl: [],
  });

  const [newAchievementEn, setNewAchievementEn] = useState('');
  const [newAchievementPl, setNewAchievementPl] = useState('');

  const { data: expData, isLoading: expLoading } = useQuery({
    queryKey: queryKeys.admin.experience(id!),
    queryFn: () => adminExperiencesApi.getById(id!),
    enabled: isEditing,
  });

  useEffect(() => {
    if (expData?.data) {
      const exp = expData.data;
      setFormData({
        titleEn: exp.titleEn,
        titlePl: exp.titlePl,
        company: exp.company,
        startDate: exp.startDate,
        endDate: exp.endDate || '',
        descriptionEn: exp.descriptionEn || '',
        descriptionPl: exp.descriptionPl || '',
        achievementsEn: exp.achievementsEn || [],
        achievementsPl: exp.achievementsPl || [],
      });
    }
  }, [expData]);

  const createMutation = useMutation({
    mutationFn: (data: ExperienceCreateRequest) => adminExperiencesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.experiences() });
      queryClient.invalidateQueries({ queryKey: ['experiences'] });
      toast.success('Experience created successfully');
      navigate('/admin/experiences');
    },
    onError: () => {
      toast.error('Failed to create experience');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: ExperienceCreateRequest) => adminExperiencesApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.experiences() });
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.experience(id!) });
      queryClient.invalidateQueries({ queryKey: ['experiences'] });
      toast.success('Experience updated successfully');
      navigate('/admin/experiences');
    },
    onError: () => {
      toast.error('Failed to update experience');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const submitData = {
      ...formData,
      endDate: formData.endDate || undefined,
    };
    if (isEditing) {
      updateMutation.mutate(submitData);
    } else {
      createMutation.mutate(submitData);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const addAchievementEn = () => {
    if (newAchievementEn.trim()) {
      setFormData((prev) => ({
        ...prev,
        achievementsEn: [...(prev.achievementsEn || []), newAchievementEn.trim()],
      }));
      setNewAchievementEn('');
    }
  };

  const addAchievementPl = () => {
    if (newAchievementPl.trim()) {
      setFormData((prev) => ({
        ...prev,
        achievementsPl: [...(prev.achievementsPl || []), newAchievementPl.trim()],
      }));
      setNewAchievementPl('');
    }
  };

  const removeAchievementEn = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      achievementsEn: prev.achievementsEn?.filter((_, i) => i !== index) || [],
    }));
  };

  const removeAchievementPl = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      achievementsPl: prev.achievementsPl?.filter((_, i) => i !== index) || [],
    }));
  };

  if (expLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/admin/experiences')}
          className="p-2 text-dark-400 hover:text-dark-100 hover:bg-dark-700 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {isEditing ? t('admin.experiences.editExperience') : t('admin.experiences.newExperience')}
        </h1>
      </div>

      {/* Form */}
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

        {/* Company */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Company</h3>
          <div>
            <label className="label">Company *</label>
            <input
              type="text"
              name="company"
              value={formData.company}
              onChange={handleChange}
              className="input"
              required
            />
          </div>
        </div>

        {/* Dates */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Dates</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Start Date *</label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="input"
                required
              />
            </div>
            <div>
              <label className="label">End Date (leave empty for current position)</label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                className="input"
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
                rows={4}
              />
            </div>
            <div>
              <label className="label">Description (Polish)</label>
              <textarea
                name="descriptionPl"
                value={formData.descriptionPl}
                onChange={handleChange}
                className="input"
                rows={4}
              />
            </div>
          </div>
        </div>

        {/* Achievements */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Achievements</h3>
          <div className="grid md:grid-cols-2 gap-6">
            {/* English Achievements */}
            <div>
              <label className="label">Achievements (English)</label>
              <div className="space-y-2 mb-3">
                {formData.achievementsEn?.map((achievement, index) => (
                  <div key={index} className="flex items-center space-x-2 bg-dark-700 rounded-lg px-3 py-2">
                    <span className="flex-1 text-sm text-dark-200">{achievement}</span>
                    <button
                      type="button"
                      onClick={() => removeAchievementEn(index)}
                      className="text-dark-400 hover:text-red-400"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={newAchievementEn}
                  onChange={(e) => setNewAchievementEn(e.target.value)}
                  className="input flex-1"
                  placeholder="Add achievement..."
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addAchievementEn())}
                />
                <button
                  type="button"
                  onClick={addAchievementEn}
                  className="btn-secondary"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Polish Achievements */}
            <div>
              <label className="label">Achievements (Polish)</label>
              <div className="space-y-2 mb-3">
                {formData.achievementsPl?.map((achievement, index) => (
                  <div key={index} className="flex items-center space-x-2 bg-dark-700 rounded-lg px-3 py-2">
                    <span className="flex-1 text-sm text-dark-200">{achievement}</span>
                    <button
                      type="button"
                      onClick={() => removeAchievementPl(index)}
                      className="text-dark-400 hover:text-red-400"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={newAchievementPl}
                  onChange={(e) => setNewAchievementPl(e.target.value)}
                  className="input flex-1"
                  placeholder="Dodaj osiągnięcie..."
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addAchievementPl())}
                />
                <button
                  type="button"
                  onClick={addAchievementPl}
                  className="btn-secondary"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => navigate('/admin/experiences')}
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
