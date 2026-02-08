import { useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useDropzone } from 'react-dropzone';
import toast from 'react-hot-toast';
import { Upload, Trash2, Image, Film, FileText, X, Youtube } from 'lucide-react';
import { clsx } from 'clsx';
import { adminMediaApi } from '@/services/api';
import { Media, MediaType } from '@/types';
import { queryKeys } from '@/lib/queryKeys';
import { getMediaUrl } from '@/lib/mediaUrl';
import LoadingSpinner from '@/components/LoadingSpinner';

const mediaTypeIcons: Record<MediaType, typeof Image> = {
  [MediaType.IMAGE]: Image,
  [MediaType.VIDEO]: Film,
  [MediaType.PDF]: FileText,
  [MediaType.YOUTUBE]: Youtube,
};

export default function AdminMedia() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [selectedMedia, setSelectedMedia] = useState<Media | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.media(),
    queryFn: () => adminMediaApi.getAll(0, 100),
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => adminMediaApi.upload(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.media() });
      toast.success(t('admin.media.upload') + ' successful');
    },
    onError: () => {
      toast.error('Failed to upload file');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminMediaApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.media() });
      setSelectedMedia(null);
      toast.success('Media deleted successfully');
    },
    onError: () => {
      toast.error('Failed to delete media');
    },
  });

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      setIsUploading(true);
      for (const file of acceptedFiles) {
        await uploadMutation.mutateAsync(file);
      }
      setIsUploading(false);
    },
    [uploadMutation]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.jpg', '.jpeg', '.png', '.gif', '.webp'],
      'video/*': ['.mp4', '.webm'],
      'application/pdf': ['.pdf'],
    },
  });

  const media = data?.data?.content || [];

  const handleDelete = (item: Media) => {
    if (window.confirm(`Are you sure you want to delete "${item.originalName}"?`)) {
      deleteMutation.mutate(item.id);
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <h1 className="text-3xl font-display font-bold text-dark-100">
        {t('admin.media.title')}
      </h1>

      {/* Upload Zone */}
      <div
        {...getRootProps()}
        className={clsx(
          'border-2 border-dashed rounded-xl p-12 text-center cursor-pointer transition-colors',
          isDragActive
            ? 'border-primary-500 bg-primary-500/10'
            : 'border-dark-600 hover:border-dark-500 hover:bg-dark-800/50'
        )}
      >
        <input {...getInputProps()} />
        <Upload className="w-12 h-12 text-dark-500 mx-auto mb-4" />
        <p className="text-dark-300 mb-2">{t('admin.media.dragDrop')}</p>
        <p className="text-dark-500 text-sm">{t('admin.media.or')}</p>
        <button className="btn-secondary mt-4">
          {isUploading ? 'Uploading...' : t('admin.media.browse')}
        </button>
      </div>

      {/* Media Grid */}
      {media.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-dark-400">{t('admin.media.noMedia')}</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
          {media.map((item) => {
            const Icon = mediaTypeIcons[item.type] || Image;
            return (
              <div
                key={item.id}
                onClick={() => setSelectedMedia(item)}
                className={clsx(
                  'group relative aspect-square rounded-lg overflow-hidden cursor-pointer border-2 transition-colors',
                  selectedMedia?.id === item.id
                    ? 'border-primary-500'
                    : 'border-transparent hover:border-dark-600'
                )}
              >
                {item.type === MediaType.IMAGE ? (
                  <img
                    src={getMediaUrl(item.url)}
                    alt={item.altText || item.originalName || ''}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full bg-dark-700 flex items-center justify-center">
                    <Icon className="w-12 h-12 text-dark-500" />
                  </div>
                )}
                <div className="absolute inset-0 bg-dark-900/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(item);
                    }}
                    className="p-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Media Detail Modal */}
      {selectedMedia && (
        <div className="fixed inset-0 bg-dark-900/80 flex items-center justify-center z-50 p-4">
          <div className="card max-w-2xl w-full max-h-[90vh] overflow-auto">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-dark-100">Media Details</h2>
              <button
                onClick={() => setSelectedMedia(null)}
                className="p-2 text-dark-400 hover:text-dark-100 hover:bg-dark-700 rounded-lg transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {selectedMedia.type === MediaType.IMAGE ? (
              <img
                src={getMediaUrl(selectedMedia.url)}
                alt={selectedMedia.altText || selectedMedia.originalName || ''}
                className="w-full rounded-lg mb-6"
              />
            ) : selectedMedia.type === MediaType.VIDEO ? (
              <video
                src={getMediaUrl(selectedMedia.url)}
                controls
                className="w-full rounded-lg mb-6"
              />
            ) : (
              <div className="w-full h-48 bg-dark-700 rounded-lg mb-6 flex items-center justify-center">
                <FileText className="w-16 h-16 text-dark-500" />
              </div>
            )}

            <div className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-dark-500">Filename:</span>
                <span className="text-dark-200">{selectedMedia.originalName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-dark-500">Type:</span>
                <span className="text-dark-200">{selectedMedia.mimeType}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-dark-500">Size:</span>
                <span className="text-dark-200">{selectedMedia.size ? formatFileSize(selectedMedia.size) : 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-dark-500">URL:</span>
                <input
                  type="text"
                  value={selectedMedia.url}
                  readOnly
                  className="input text-xs flex-1 ml-4"
                  onClick={(e) => (e.target as HTMLInputElement).select()}
                />
              </div>
            </div>

            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => handleDelete(selectedMedia)}
                className="btn-secondary text-red-400 hover:bg-red-500/20"
              >
                <Trash2 className="w-5 h-5 mr-2" />
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
