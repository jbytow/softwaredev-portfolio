import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ArrowLeft, Save, Upload, X } from 'lucide-react';
import { adminInterestsApi, adminMediaApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import { InterestCreateRequest } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import { getMediaUrl } from '@/lib/mediaUrl';

export default function AdminInterestForm() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const isEditing = Boolean(id);

  const [formData, setFormData] = useState<InterestCreateRequest>({
    titleEn: '',
    titlePl: '',
    image1: '',
    image2: '',
    image3: '',
  });
  const [uploading, setUploading] = useState<{ [key: string]: boolean }>({});

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.interest(id!),
    queryFn: () => adminInterestsApi.getById(id!),
    enabled: isEditing,
  });

  useEffect(() => {
    if (data?.data) {
      const interest = data.data;
      setFormData({
        titleEn: interest.titleEn,
        titlePl: interest.titlePl,
        image1: interest.image1 || '',
        image2: interest.image2 || '',
        image3: interest.image3 || '',
      });
    }
  }, [data]);

  const createMutation = useMutation({
    mutationFn: (data: InterestCreateRequest) => adminInterestsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.interests() });
      toast.success('Interest created');
      navigate('/admin/interests');
    },
    onError: () => {
      toast.error('Failed to create interest');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: InterestCreateRequest) => adminInterestsApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.interests() });
      toast.success('Interest updated');
      navigate('/admin/interests');
    },
    onError: () => {
      toast.error('Failed to update interest');
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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleImageUpload = async (field: 'image1' | 'image2' | 'image3', file: File) => {
    setUploading((prev) => ({ ...prev, [field]: true }));
    try {
      const response = await adminMediaApi.upload(file);
      if (response.data) {
        setFormData((prev) => ({ ...prev, [field]: response.data.url }));
      }
    } catch {
      toast.error('Failed to upload image');
    } finally {
      setUploading((prev) => ({ ...prev, [field]: false }));
    }
  };

  const clearImage = (field: 'image1' | 'image2' | 'image3') => {
    setFormData((prev) => ({ ...prev, [field]: '' }));
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate('/admin/interests')}
          className="p-2 text-dark-400 hover:text-dark-100 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-3xl font-display font-bold text-dark-100">
          {isEditing ? t('admin.interests.editInterest') : t('admin.interests.newInterest')}
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="card space-y-4">
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="label">Title (English)</label>
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
              <label className="label">Title (Polish)</label>
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

          <div>
            <label className="label">Images (up to 3)</label>
            <div className="grid grid-cols-3 gap-4">
              {(['image1', 'image2', 'image3'] as const).map((field, idx) => (
                <div key={field} className="relative">
                  {formData[field] ? (
                    <div className="relative aspect-square rounded-lg overflow-hidden bg-dark-700">
                      <img
                        src={getMediaUrl(formData[field]!)}
                        alt={`Image ${idx + 1}`}
                        className="w-full h-full object-cover"
                      />
                      <button
                        type="button"
                        onClick={() => clearImage(field)}
                        className="absolute top-2 right-2 p-1 bg-dark-900/80 rounded-full text-dark-300 hover:text-white"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ) : (
                    <label className="aspect-square rounded-lg border-2 border-dashed border-dark-600 hover:border-primary-500 flex flex-col items-center justify-center cursor-pointer transition-colors">
                      {uploading[field] ? (
                        <LoadingSpinner />
                      ) : (
                        <>
                          <Upload className="w-6 h-6 text-dark-500 mb-2" />
                          <span className="text-xs text-dark-500">Image {idx + 1}</span>
                        </>
                      )}
                      <input
                        type="file"
                        accept="image/*"
                        className="hidden"
                        onChange={(e) => {
                          const file = e.target.files?.[0];
                          if (file) handleImageUpload(field, file);
                        }}
                      />
                    </label>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="flex justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate('/admin/interests')}
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
