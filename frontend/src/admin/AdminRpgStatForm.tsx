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

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.rpgStat(id!),
    queryFn: () => adminRpgStatsApi.getById(id!),
    enabled: isEditing,
  });

  useEffect(() => {
    if (data?.data) {
      const stat = data.data;
      setFormData({
        attr: stat.attr,
        labelEn: stat.labelEn,
        labelPl: stat.labelPl,
        level: stat.level,
        maxLevel: stat.maxLevel,
        skills: stat.skills,
      });
      setSkillsText(stat.skills.join(', '));
    }
  }, [data]);

  const createMutation = useMutation({
    mutationFn: (data: RpgStatCreateRequest) => adminRpgStatsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.rpgStats() });
      toast.success('RPG stat created');
      navigate('/admin/rpg-stats');
    },
    onError: () => {
      toast.error('Failed to create RPG stat');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: RpgStatCreateRequest) => adminRpgStatsApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.rpgStats() });
      toast.success('RPG stat updated');
      navigate('/admin/rpg-stats');
    },
    onError: () => {
      toast.error('Failed to update RPG stat');
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

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/admin/rpg-stats')}
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
          <h3 className="text-lg font-semibold text-dark-100">Attribute</h3>
          <div className="grid md:grid-cols-3 gap-4">
            <div>
              <label className="label">Attribute Code *</label>
              <input
                type="text"
                name="attr"
                value={formData.attr}
                onChange={handleChange}
                className="input font-mono"
                placeholder="e.g., STR, INT, WIS"
                required
                maxLength={10}
              />
            </div>
            <div>
              <label className="label">Label (English) *</label>
              <input
                type="text"
                name="labelEn"
                value={formData.labelEn}
                onChange={handleChange}
                className="input"
                placeholder="e.g., Java / Spring"
                required
              />
            </div>
            <div>
              <label className="label">Label (Polish) *</label>
              <input
                type="text"
                name="labelPl"
                value={formData.labelPl}
                onChange={handleChange}
                className="input"
                placeholder="e.g., Java / Spring"
                required
              />
            </div>
          </div>
        </div>

        {/* Level */}
        <div className="card space-y-4">
          <h3 className="text-lg font-semibold text-dark-100">Level</h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Current Level</label>
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
              <label className="label">Max Level</label>
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
          <h3 className="text-lg font-semibold text-dark-100">Related Skills</h3>
          <div>
            <label className="label">Skills (comma-separated)</label>
            <input
              type="text"
              value={skillsText}
              onChange={(e) => setSkillsText(e.target.value)}
              className="input"
              placeholder="e.g., Spring Boot, Hibernate, REST API, JUnit"
            />
            <p className="text-sm text-dark-500 mt-2">
              Enter skill names separated by commas
            </p>
          </div>
        </div>

        <div className="flex justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate('/admin/rpg-stats')}
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
