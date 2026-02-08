import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ArrowLeft, Save } from 'lucide-react';
import { adminSoftSkillsApi, adminSkillCategoriesApi } from '@/services/api';
import { SoftSkillCreateRequest } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminSoftSkillForm() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditing = !!id;

  const [formData, setFormData] = useState<SoftSkillCreateRequest>({
    nameEn: '',
    namePl: '',
    descriptionEn: '',
    descriptionPl: '',
    professionalUsageEn: '',
    professionalUsagePl: '',
    icon: '',
    categoryId: '',
  });

  const { data: skillData, isLoading: skillLoading } = useQuery({
    queryKey: queryKeys.admin.softSkill(id!),
    queryFn: () => adminSoftSkillsApi.getById(id!),
    enabled: isEditing,
  });

  const { data: categoriesData } = useQuery({
    queryKey: queryKeys.admin.skillCategories(),
    queryFn: () => adminSkillCategoriesApi.getAll(),
  });

  const categories = categoriesData?.data || [];

  useEffect(() => {
    if (skillData?.data) {
      const skill = skillData.data;
      setFormData({
        nameEn: skill.nameEn,
        namePl: skill.namePl,
        descriptionEn: skill.descriptionEn || '',
        descriptionPl: skill.descriptionPl || '',
        professionalUsageEn: skill.professionalUsageEn || '',
        professionalUsagePl: skill.professionalUsagePl || '',
        icon: skill.icon || '',
        categoryId: skill.categoryId || '',
      });
    }
  }, [skillData]);

  const createMutation = useMutation({
    mutationFn: (data: SoftSkillCreateRequest) => adminSoftSkillsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkills() });
      queryClient.invalidateQueries({ queryKey: ['softSkills'] });
      toast.success('Soft skill created successfully');
      navigate('/admin/soft-skills');
    },
    onError: () => {
      toast.error('Failed to create soft skill');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: SoftSkillCreateRequest) => adminSoftSkillsApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkills() });
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkill(id!) });
      queryClient.invalidateQueries({ queryKey: ['softSkills'] });
      toast.success('Soft skill updated successfully');
      navigate('/admin/soft-skills');
    },
    onError: () => {
      toast.error('Failed to update soft skill');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const submitData = {
      ...formData,
      categoryId: formData.categoryId || undefined,
    };
    if (isEditing) {
      updateMutation.mutate(submitData);
    } else {
      createMutation.mutate(submitData);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  if (skillLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/admin/soft-skills')}
          className="p-2 text-dark-400 hover:text-dark-100 hover:bg-dark-700 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {isEditing ? t('admin.softSkills.editSkill') : t('admin.softSkills.newSkill')}
        </h1>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Names */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Names</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Name (English) *</label>
              <input
                type="text"
                name="nameEn"
                value={formData.nameEn}
                onChange={handleChange}
                className="input"
                required
              />
            </div>
            <div>
              <label className="label">Name (Polish) *</label>
              <input
                type="text"
                name="namePl"
                value={formData.namePl}
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

        {/* Professional Usage */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Professional Usage</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Professional Usage (English)</label>
              <textarea
                name="professionalUsageEn"
                value={formData.professionalUsageEn}
                onChange={handleChange}
                className="input"
                rows={3}
                placeholder="How do you apply this skill professionally?"
              />
            </div>
            <div>
              <label className="label">Professional Usage (Polish)</label>
              <textarea
                name="professionalUsagePl"
                value={formData.professionalUsagePl}
                onChange={handleChange}
                className="input"
                rows={3}
                placeholder="Jak stosujesz tę umiejętność zawodowo?"
              />
            </div>
          </div>
        </div>

        {/* Category & Icon */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Category & Icon</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Category</label>
              <select
                name="categoryId"
                value={formData.categoryId}
                onChange={handleChange}
                className="input"
              >
                <option value="">No category</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.nameEn}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Icon Name</label>
              <input
                type="text"
                name="icon"
                value={formData.icon}
                onChange={handleChange}
                className="input"
                placeholder="e.g., lightbulb, users, target, brain, heart"
              />
              <p className="text-sm text-dark-500 mt-2">
                lightbulb, users, message, target, clock, heart, zap, brain, handshake, rocket, sparkles, award
              </p>
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => navigate('/admin/soft-skills')}
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
