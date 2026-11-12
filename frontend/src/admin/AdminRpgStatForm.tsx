import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ArrowLeft, Save } from 'lucide-react';
import { adminRpgStatsApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import { RpgStatCreateRequest } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import LoadError from '@/admin/components/LoadError';
import { useUnsavedChanges } from './hooks/useUnsavedChanges';
import { getApiErrorMessage } from '@/lib/apiError';

export default function AdminRpgStatForm() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditing = Boolean(id);

  const [formData, setFormData] = useState<RpgStatCreateRequest>({
    attr: '',
    labelEn: '',
    labelPl: '',
    level: 5,
    maxLevel: 10,
    skills: [],
  });
  const [skillsText, setSkillsText] = useState('');
  const [baseline, setBaseline] = useState(() => JSON.stringify({ formData, skillsText }));
  const { confirmLeave } = useUnsavedChanges(JSON.stringify({ formData, skillsText }) !== baseline);

  const { data, isLoading, isError: loadFailed, refetch } = useQuery({
    queryKey: queryKeys.admin.rpgStat(id!),
    queryFn: () => adminRpgStatsApi.getById(id!),
    enabled: isEditing,
  });

  useEffect(() => {
    if (data?.data) {
      const stat = data.data;
      const seeded: typeof formData = {
        attr: stat.attr,
        labelEn: stat.labelEn,
        labelPl: stat.labelPl,
        level: stat.level,
        maxLevel: stat.maxLevel,
        skills: stat.skills,
      };
      setFormData(seeded);
      setBaseline(JSON.stringify({ formData: seeded, skillsText: stat.skills.join(', ') }));
      setSkillsText(stat.skills.join(', '));
    }
  }, [data]);

  const createMutation = useMutation({
    mutationFn: (data: RpgStatCreateRequest) => adminRpgStatsApi.create(data),
    onSuccess: () => {
      setBaseline(JSON.stringify({ formData, skillsText }));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.rpgStats() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.rpgStats() });
      toast.success(t('admin.rpgStats.created'));
      navigate('/admin/rpg-stats');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.rpgStats.createFailed')));
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: RpgStatCreateRequest) => adminRpgStatsApi.update(id!, data),
    onSuccess: () => {
      setBaseline(JSON.stringify({ formData, skillsText }));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.rpgStats() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.rpgStats() });
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.rpgStat(id!) });
      toast.success(t('admin.rpgStats.updated'));
      navigate('/admin/rpg-stats');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.rpgStats.updateFailed')));
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const skills = skillsText
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
    const submitData = { ...formData, skills };
    if (isEditing) {
      updateMutation.mutate(submitData);
    } else {
      createMutation.mutate(submitData);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (loadFailed && !data) {
    return <LoadError onRetry={() => refetch()} />;
  }

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button
          onClick={() => confirmLeave() && navigate('/admin/rpg-stats')}
          className="p-2 text-dark-400 hover:text-dark-100 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {isEditing ? t('admin.rpgStats.editStat') : t('admin.rpgStats.newStat')}
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Attribute & Labels */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">
            {t('admin.rpgStats.attributeHeading')}
          </h3>
          <div className="grid md:grid-cols-3 gap-4">
            <div>
              <label className="label">{t('admin.rpgStats.attrCodeLabel')}</label>
              <input
                type="text"
                name="attr"
                value={formData.attr}
                onChange={handleChange}
                className="input font-mono"
                placeholder={t('admin.rpgStats.attrPlaceholder')}
                required
                maxLength={10}
              />
            </div>
            <div>
              <label className="label">{t('admin.rpgStats.labelEnLabel')}</label>
              <input
                type="text"
                name="labelEn"
                value={formData.labelEn}
                onChange={handleChange}
                className="input"
                placeholder={t('admin.rpgStats.labelPlaceholder')}
                required
              />
            </div>
            <div>
              <label className="label">{t('admin.rpgStats.labelPlLabel')}</label>
              <input
                type="text"
                name="labelPl"
                value={formData.labelPl}
                onChange={handleChange}
                className="input"
                placeholder={t('admin.rpgStats.labelPlaceholder')}
                required
              />
            </div>
          </div>
        </div>

        {/* Level */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">
            {t('admin.rpgStats.levelHeading')}
          </h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.rpgStats.currentLevel')}</label>
              <input
                type="number"
                name="level"
                value={formData.level}
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    level: Math.max(1, parseInt(e.target.value) || 1),
                  }))
                }
                className="input"
                min={1}
              />
            </div>
            <div>
              <label className="label">{t('admin.rpgStats.maxLevel')}</label>
              <input
                type="number"
                name="maxLevel"
                value={formData.maxLevel}
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    maxLevel: Math.max(1, parseInt(e.target.value) || 10),
                  }))
                }
                className="input"
                min={1}
              />
            </div>
          </div>
        </div>

        {/* Skills */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">
            {t('admin.rpgStats.skillsHeading')}
          </h3>
          <div>
            <label className="label">{t('admin.rpgStats.skillsLabel')}</label>
            <input
              type="text"
              value={skillsText}
              onChange={(e) => setSkillsText(e.target.value)}
              className="input"
              placeholder={t('admin.rpgStats.skillsPlaceholder')}
            />
            <p className="text-sm text-dark-500 mt-2">
              {t('admin.rpgStats.skillsHint')}
            </p>
          </div>
        </div>

        <div className="flex justify-end gap-4">
          <button
            type="button"
            onClick={() => confirmLeave() && navigate('/admin/rpg-stats')}
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
