import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { Save, Plus, Trash2 } from 'lucide-react';
import { adminSettingsApi } from '@/services/api';
import { SiteSettingsUpdateRequest, StatItemInput } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';
import LoadError from '@/admin/components/LoadError';
import { useUnsavedChanges } from './hooks/useUnsavedChanges';
import { getApiErrorMessage } from '@/lib/apiError';

export default function AdminSettings() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [formData, setFormData] = useState<SiteSettingsUpdateRequest>({
    heroTitleEn: '',
    heroTitlePl: '',
    rpgClassTitleEn: '',
    rpgClassTitlePl: '',
    heroSubtitleEn: '',
    heroSubtitlePl: '',
    aboutTextEn: '',
    aboutTextPl: '',
    profileImage: '',
    email: '',
    phone: '',
    socialLinks: {},
    metaDescriptionEn: '',
    metaDescriptionPl: '',
    footerTitleEn: '',
    footerTitlePl: '',
    footerTaglineEn: '',
    footerTaglinePl: '',
    ownerName: '',
    siteName: '',
    aboutTagsEn: [],
    aboutTagsPl: [],
    statsItems: [],
  });
  const [baseline, setBaseline] = useState(() => JSON.stringify(formData));
  useUnsavedChanges(JSON.stringify(formData) !== baseline);

  const { data, isLoading, isError: loadFailed, refetch } = useQuery({
    queryKey: queryKeys.admin.settings(),
    queryFn: () => adminSettingsApi.get(),
  });

  useEffect(() => {
    if (data?.data) {
      const settings = data.data;
      const seeded: typeof formData = {
        heroTitleEn: settings.heroTitleEn || '',
        heroTitlePl: settings.heroTitlePl || '',
        rpgClassTitleEn: settings.rpgClassTitleEn || '',
        rpgClassTitlePl: settings.rpgClassTitlePl || '',
        heroSubtitleEn: settings.heroSubtitleEn || '',
        heroSubtitlePl: settings.heroSubtitlePl || '',
        aboutTextEn: settings.aboutTextEn || '',
        aboutTextPl: settings.aboutTextPl || '',
        profileImage: settings.profileImage || '',
        email: settings.email || '',
        phone: settings.phone || '',
        socialLinks: settings.socialLinks || {},
        metaDescriptionEn: settings.metaDescriptionEn || '',
        metaDescriptionPl: settings.metaDescriptionPl || '',
        footerTitleEn: settings.footerTitleEn || '',
        footerTitlePl: settings.footerTitlePl || '',
        footerTaglineEn: settings.footerTaglineEn || '',
        footerTaglinePl: settings.footerTaglinePl || '',
        ownerName: settings.ownerName || '',
        siteName: settings.siteName || '',
        aboutTagsEn: settings.aboutTagsEn || [],
        aboutTagsPl: settings.aboutTagsPl || [],
        statsItems: settings.statsItems?.map(s => ({
          icon: s.icon,
          value: s.value,
          labelEn: s.labelEn,
          labelPl: s.labelPl,
        })) || [],
      };
      setFormData(seeded);
      setBaseline(JSON.stringify(seeded));
    }
  }, [data]);

  const updateMutation = useMutation({
    mutationFn: (data: SiteSettingsUpdateRequest) => adminSettingsApi.update(data),
    onSuccess: () => {
      setBaseline(JSON.stringify(formData));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.settings() });
      // Invalidate all public settings queries (matches all languages)
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.settings() });
      toast.success(t('admin.settings.saved'));
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.settings.saveFailed')));
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(formData);
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

  const handleSocialLinkChange = (platform: string, url: string) => {
    setFormData((prev) => ({
      ...prev,
      socialLinks: {
        ...prev.socialLinks,
        [platform]: url,
      },
    }));
  };

  const handleStatChange = (index: number, field: keyof StatItemInput, value: string) => {
    setFormData((prev) => ({
      ...prev,
      statsItems: prev.statsItems?.map((item, i) =>
        i === index ? { ...item, [field]: value } : item
      ),
    }));
  };

  const addStat = () => {
    setFormData((prev) => ({
      ...prev,
      statsItems: [...(prev.statsItems || []), { icon: 'target', value: '', labelEn: '', labelPl: '' }],
    }));
  };

  const removeStat = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      statsItems: prev.statsItems?.filter((_, i) => i !== index),
    }));
  };

  const availableIcons = [
    'target', 'users', 'trending-up', 'sparkles', 'lightbulb', 'message',
    'clock', 'heart', 'zap', 'brain', 'handshake', 'rocket', 'award',
    'star', 'trophy', 'briefcase', 'globe'
  ];

  const [newTagEn, setNewTagEn] = useState('');
  const [newTagPl, setNewTagPl] = useState('');

  const addTag = () => {
    if (newTagEn.trim() && newTagPl.trim()) {
      setFormData((prev) => ({
        ...prev,
        aboutTagsEn: [...(prev.aboutTagsEn || []), newTagEn.trim()],
        aboutTagsPl: [...(prev.aboutTagsPl || []), newTagPl.trim()],
      }));
      setNewTagEn('');
      setNewTagPl('');
    }
  };

  const removeTag = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      aboutTagsEn: prev.aboutTagsEn?.filter((_, i) => i !== index),
      aboutTagsPl: prev.aboutTagsPl?.filter((_, i) => i !== index),
    }));
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (loadFailed && !data) {
    return <LoadError onRetry={() => refetch()} />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <h1 className="text-3xl font-display font-bold text-dark-100">
        {t('admin.settings.title')}
      </h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* General */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.general')}
          </h2>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.siteName')}</label>
              <input
                type="text"
                name="siteName"
                value={formData.siteName}
                onChange={handleChange}
                className="input"
                placeholder="My Portfolio"
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.ownerName')}</label>
              <input
                type="text"
                name="ownerName"
                value={formData.ownerName}
                onChange={handleChange}
                className="input"
                placeholder="Jane Doe"
              />
            </div>
          </div>
        </div>

        {/* Hero Section */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.hero')}
          </h2>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.heroTitleEn')}</label>
              <input
                type="text"
                name="heroTitleEn"
                value={formData.heroTitleEn}
                onChange={handleChange}
                className="input"
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.heroTitlePl')}</label>
              <input
                type="text"
                name="heroTitlePl"
                value={formData.heroTitlePl}
                onChange={handleChange}
                className="input"
              />
            </div>
          </div>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.heroSubtitleEn')}</label>
              <textarea
                name="heroSubtitleEn"
                value={formData.heroSubtitleEn}
                onChange={handleChange}
                className="input"
                rows={2}
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.heroSubtitlePl')}</label>
              <textarea
                name="heroSubtitlePl"
                value={formData.heroSubtitlePl}
                onChange={handleChange}
                className="input"
                rows={2}
              />
            </div>
          </div>
        </div>

        {/* Stats Section */}
        <div className="card space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-dark-100">
              {t('admin.settings.stats')}
            </h2>
            <button
              type="button"
              onClick={addStat}
              className="btn-secondary text-sm"
            >
              <Plus className="w-4 h-4 mr-1" />
              {t('admin.settings.addStat')}
            </button>
          </div>
          <div className="space-y-4">
            {formData.statsItems?.map((stat, index) => (
              <div key={index} className="p-4 bg-dark-700 rounded-lg space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-dark-300">
                    {t('admin.settings.statNumber', { number: index + 1 })}
                  </span>
                  <button
                    type="button"
                    onClick={() => removeStat(index)}
                    className="text-red-400 hover:text-red-300"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
                <div className="grid md:grid-cols-2 gap-3">
                  <div>
                    <label className="label text-xs">{t('admin.settings.icon')}</label>
                    <select
                      value={stat.icon}
                      onChange={(e) => handleStatChange(index, 'icon', e.target.value)}
                      className="input"
                    >
                      {availableIcons.map((icon) => (
                        <option key={icon} value={icon}>
                          {icon}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="label text-xs">{t('admin.settings.statValue')}</label>
                    <input
                      type="text"
                      value={stat.value}
                      onChange={(e) => handleStatChange(index, 'value', e.target.value)}
                      className="input"
                      placeholder="50+"
                    />
                  </div>
                </div>
                <div className="grid md:grid-cols-2 gap-3">
                  <div>
                    <label className="label text-xs">{t('admin.settings.statLabelEn')}</label>
                    <input
                      type="text"
                      value={stat.labelEn}
                      onChange={(e) => handleStatChange(index, 'labelEn', e.target.value)}
                      className="input"
                      placeholder="Campaigns"
                    />
                  </div>
                  <div>
                    <label className="label text-xs">{t('admin.settings.statLabelPl')}</label>
                    <input
                      type="text"
                      value={stat.labelPl}
                      onChange={(e) => handleStatChange(index, 'labelPl', e.target.value)}
                      className="input"
                      placeholder="Kampanii"
                    />
                  </div>
                </div>
              </div>
            ))}
            {(!formData.statsItems || formData.statsItems.length === 0) && (
              <p className="text-dark-500 text-center py-4">
                {t('admin.settings.noStats')}
              </p>
            )}
          </div>
        </div>

        {/* About Section */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.about')}
          </h2>
          <div>
            <label className="label">{t('admin.settings.profileImageUrl')}</label>
            <input
              type="text"
              name="profileImage"
              value={formData.profileImage}
              onChange={handleChange}
              className="input"
              placeholder="https://..."
            />
            {formData.profileImage && (
              <img
                src={formData.profileImage}
                alt={t('admin.settings.profilePreviewAlt')}
                className="mt-4 w-32 h-32 rounded-full object-cover"
              />
            )}
          </div>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.aboutTextEn')}</label>
              <textarea
                name="aboutTextEn"
                value={formData.aboutTextEn}
                onChange={handleChange}
                className="input"
                rows={6}
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.aboutTextPl')}</label>
              <textarea
                name="aboutTextPl"
                value={formData.aboutTextPl}
                onChange={handleChange}
                className="input"
                rows={6}
              />
            </div>
          </div>

          {/* About Tags */}
          <div className="pt-4 border-t border-dark-700">
            <label className="label">{t('admin.settings.skillTags')}</label>
            <div className="flex flex-wrap gap-2 mb-4">
              {formData.aboutTagsEn?.map((tag, index) => (
                <span
                  key={index}
                  className="inline-flex items-center gap-1 px-3 py-1 bg-primary-500/20 text-primary-400 rounded-full text-sm"
                >
                  {tag} / {formData.aboutTagsPl?.[index]}
                  <button
                    type="button"
                    onClick={() => removeTag(index)}
                    className="hover:text-red-400"
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
            <div className="grid grid-cols-3 gap-2">
              <input
                type="text"
                value={newTagEn}
                onChange={(e) => setNewTagEn(e.target.value)}
                placeholder={t('admin.settings.tagPlaceholderEn')}
                className="input"
              />
              <input
                type="text"
                value={newTagPl}
                onChange={(e) => setNewTagPl(e.target.value)}
                placeholder={t('admin.settings.tagPlaceholderPl')}
                className="input"
              />
              <button
                type="button"
                onClick={addTag}
                className="btn-secondary"
              >
                {t('admin.settings.addTag')}
              </button>
            </div>
          </div>
        </div>

        {/* RPG / Character Sheet */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.rpg')}
          </h2>
          <p className="text-sm text-dark-400">
            {t('admin.settings.rpgDescription')}
          </p>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.classTitleEn')}</label>
              <input
                type="text"
                name="rpgClassTitleEn"
                value={formData.rpgClassTitleEn}
                onChange={handleChange}
                className="input"
                placeholder="Level 12 Finance Wizard turned Code Knight"
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.classTitlePl')}</label>
              <input
                type="text"
                name="rpgClassTitlePl"
                value={formData.rpgClassTitlePl}
                onChange={handleChange}
                className="input"
                placeholder="Poziom 12 Czarodziej Finansów przekwalifikowany na Rycerza Kodu"
              />
            </div>
          </div>
        </div>

        {/* Contact Information */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.contact')}
          </h2>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.email')}</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="input"
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.phone')}</label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="input"
              />
            </div>
          </div>
        </div>

        {/* Social Links */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.social')}
          </h2>
          <div className="grid md:grid-cols-2 gap-4">
            {['linkedin', 'instagram', 'facebook'].map((platform) => (
              <div key={platform}>
                <label className="label capitalize">{platform}</label>
                <input
                  type="url"
                  value={formData.socialLinks?.[platform] || ''}
                  onChange={(e) => handleSocialLinkChange(platform, e.target.value)}
                  className="input"
                  placeholder={`https://${platform}.com/...`}
                />
              </div>
            ))}
          </div>
        </div>

        {/* Footer Section */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.footer')}
          </h2>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.footerTitleEn')}</label>
              <input
                type="text"
                name="footerTitleEn"
                value={formData.footerTitleEn}
                onChange={handleChange}
                className="input"
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.footerTitlePl')}</label>
              <input
                type="text"
                name="footerTitlePl"
                value={formData.footerTitlePl}
                onChange={handleChange}
                className="input"
              />
            </div>
          </div>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.footerTaglineEn')}</label>
              <textarea
                name="footerTaglineEn"
                value={formData.footerTaglineEn}
                onChange={handleChange}
                className="input"
                rows={2}
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.footerTaglinePl')}</label>
              <textarea
                name="footerTaglinePl"
                value={formData.footerTaglinePl}
                onChange={handleChange}
                className="input"
                rows={2}
              />
            </div>
          </div>
        </div>

        {/* SEO Settings */}
        <div className="card space-y-4">
          <h2 className="text-xl font-semibold text-dark-100">
            {t('admin.settings.seo')}
          </h2>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">{t('admin.settings.metaDescriptionEn')}</label>
              <textarea
                name="metaDescriptionEn"
                value={formData.metaDescriptionEn}
                onChange={handleChange}
                className="input"
                rows={3}
              />
            </div>
            <div>
              <label className="label">{t('admin.settings.metaDescriptionPl')}</label>
              <textarea
                name="metaDescriptionPl"
                value={formData.metaDescriptionPl}
                onChange={handleChange}
                className="input"
                rows={3}
              />
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end">
          <button
            type="submit"
            disabled={updateMutation.isPending}
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
