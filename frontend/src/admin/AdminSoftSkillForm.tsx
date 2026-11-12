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
import LoadError from '@/admin/components/LoadError';
import { useUnsavedChanges } from './hooks/useUnsavedChanges';
import { getApiErrorMessage } from '@/lib/apiError';

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
    level: 5,
    categoryId: '',
  });
  const [baseline, setBaseline] = useState(() => JSON.stringify(formData));
  const { confirmLeave } = useUnsavedChanges(JSON.stringify(formData) !== baseline);

  const { data: skillData, isLoading: skillLoading, isError: loadFailed, refetch } = useQuery({
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
      const seeded: typeof formData = {
        nameEn: skill.nameEn,
        namePl: skill.namePl,
        descriptionEn: skill.descriptionEn || '',
        descriptionPl: skill.descriptionPl || '',
        professionalUsageEn: skill.professionalUsageEn || '',
        professionalUsagePl: skill.professionalUsagePl || '',
        icon: skill.icon || '',
        level: skill.level ?? 5,
        categoryId: skill.categoryId || '',
      };
      setFormData(seeded);
      setBaseline(JSON.stringify(seeded));
    }
  }, [skillData]);

  const createMutation = useMutation({
    mutationFn: (data: SoftSkillCreateRequest) => adminSoftSkillsApi.create(data),
    onSuccess: () => {
      setBaseline(JSON.stringify(formData));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkills() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.skills() });
      toast.success(t('admin.softSkills.created'));
      navigate('/admin/soft-skills');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.softSkills.createFailed')));
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: SoftSkillCreateRequest) => adminSoftSkillsApi.update(id!, data),
    onSuccess: () => {
      setBaseline(JSON.stringify(formData));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkills() });
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.softSkill(id!) });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.skills() });
      toast.success(t('admin.softSkills.updated'));
      navigate('/admin/soft-skills');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.softSkills.updateFailed')));
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const submitData = {
      ...formData,
      // null (not undefined) tells the backend to clear the category; undefined is dropped from the JSON
      categoryId: formData.categoryId || null,
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

  if (loadFailed && !skillData) {
    return <LoadError onRetry={() => refetch()} />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => confirmLeave() && navigate('/admin/soft-skills')}
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
          <h3 className="text-lg font-semibold text-dark-100">
            {t('admin.softSkills.namesSection')}
          </h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.softSkills.nameEn')}</label>
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
              <label className="label">{t('admin.softSkills.namePl')}</label>
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
          <h3 className="text-lg font-semibold text-dark-100">
            {t('admin.softSkills.descriptionsSection')}
          </h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.softSkills.descriptionEn')}</label>
              <textarea
                name="descriptionEn"
                value={formData.descriptionEn}
                onChange={handleChange}
                className="input"
                rows={4}
              />
            </div>
            <div>
              <label className="label">{t('admin.softSkills.descriptionPl')}</label>
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
          <h3 className="text-lg font-semibold text-dark-100">
            {t('admin.softSkills.professionalUsageSection')}
          </h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.softSkills.professionalUsageEn')}</label>
              <textarea
                name="professionalUsageEn"
                value={formData.professionalUsageEn}
                onChange={handleChange}
                className="input"
                rows={3}
                placeholder={t('admin.softSkills.professionalUsageEnPlaceholder')}
              />
            </div>
            <div>
              <label className="label">{t('admin.softSkills.professionalUsagePl')}</label>
              <textarea
                name="professionalUsagePl"
                value={formData.professionalUsagePl}
                onChange={handleChange}
                className="input"
                rows={3}
                placeholder={t('admin.softSkills.professionalUsagePlPlaceholder')}
              />
            </div>
          </div>
        </div>

        {/* Category, Icon & Level */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">
            {t('admin.softSkills.categoryIconLevelSection')}
          </h3>
          <div className="grid md:grid-cols-3 gap-4">
            <div>
              <label className="label">{t('admin.softSkills.categoryLabel')}</label>
              <select
                name="categoryId"
                value={formData.categoryId ?? ''}
                onChange={handleChange}
                className="input"
              >
                <option value="">{t('admin.softSkills.noCategory')}</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.nameEn}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">{t('admin.softSkills.iconName')}</label>
              <input
                type="text"
                name="icon"
                value={formData.icon}
                onChange={handleChange}
                className="input"
                placeholder={t('admin.softSkills.iconPlaceholder')}
              />
              <p className="text-sm text-dark-500 mt-2">
                lightbulb, users, message, target, clock, heart, zap, brain, handshake, rocket, sparkles, award
              </p>
            </div>
            <div>
              <label className="label">{t('admin.softSkills.xpLevel')}</label>
              <input
                type="number"
                name="level"
                value={formData.level}
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    level: Math.max(1, Math.min(10, parseInt(e.target.value) || 5)),
                  }))
                }
                className="input"
                min={1}
                max={10}
              />
              <p className="text-sm text-dark-500 mt-2">
                {t('admin.softSkills.xpLevelHint')}
              </p>
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => confirmLeave() && navigate('/admin/soft-skills')}
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
