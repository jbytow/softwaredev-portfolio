import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ArrowLeft, Save } from 'lucide-react';
import { adminSkillCategoriesApi } from '@/services/api';
import { SkillCategoryCreateRequest } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function AdminSkillCategoryForm() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditing = !!id;

  const [formData, setFormData] = useState<SkillCategoryCreateRequest>({
    nameEn: '',
    namePl: '',
  });

  const { data: categoryData, isLoading: categoryLoading } = useQuery({
    queryKey: queryKeys.admin.skillCategory(id!),
    queryFn: () => adminSkillCategoriesApi.getById(id!),
    enabled: isEditing,
  });

  useEffect(() => {
    if (categoryData?.data) {
      const category = categoryData.data;
      setFormData({
        nameEn: category.nameEn,
        namePl: category.namePl,
      });
    }
  }, [categoryData]);

  const createMutation = useMutation({
    mutationFn: (data: SkillCategoryCreateRequest) => adminSkillCategoriesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.skillCategories() });
      queryClient.invalidateQueries({ queryKey: ['skillCategories'] });
      toast.success('Category created successfully');
      navigate('/admin/skill-categories');
    },
    onError: () => {
      toast.error('Failed to create category');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: SkillCategoryCreateRequest) => adminSkillCategoriesApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.skillCategories() });
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.skillCategory(id!) });
      queryClient.invalidateQueries({ queryKey: ['skillCategories'] });
      toast.success('Category updated successfully');
      navigate('/admin/skill-categories');
    },
    onError: () => {
      toast.error('Failed to update category');
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

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  if (categoryLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/admin/skill-categories')}
          className="p-2 text-dark-400 hover:text-dark-100 hover:bg-dark-700 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {isEditing ? t('admin.skillCategories.editCategory') : t('admin.skillCategories.newCategory')}
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

        {/* Submit */}
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => navigate('/admin/skill-categories')}
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
