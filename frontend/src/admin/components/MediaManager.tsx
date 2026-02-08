import { useState, useCallback } from 'react';
import { useMutation } from '@tanstack/react-query';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  rectSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Upload, Youtube, X, GripVertical, Image as ImageIcon } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminMediaApi } from '@/services/api';
import { Media, MediaType } from '@/types';
import { getMediaUrl } from '@/lib/mediaUrl';

interface MediaManagerProps {
  postId: string;
  media: Media[];
  onMediaChange: (media: Media[]) => void;
}

interface SortableMediaItemProps {
  item: Media;
  onDelete: (id: string) => void;
}

function SortableMediaItem({ item, onDelete }: SortableMediaItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: item.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`relative group bg-dark-700 rounded-lg overflow-hidden ${
        isDragging ? 'z-50 shadow-xl' : ''
      }`}
    >
      {/* Drag handle */}
      <div
        {...attributes}
        {...listeners}
        className="absolute top-2 left-2 p-1 rounded bg-dark-900/70 text-white opacity-0 group-hover:opacity-100 transition-opacity cursor-grab active:cursor-grabbing z-10"
      >
        <GripVertical className="w-4 h-4" />
      </div>

      {/* Delete button */}
      <button
        onClick={() => onDelete(item.id)}
        className="absolute top-2 right-2 p-1 rounded bg-red-500/70 text-white opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-500 z-10"
      >
        <X className="w-4 h-4" />
      </button>

      {/* Media preview */}
      <div className="aspect-video">
        {item.type === MediaType.YOUTUBE ? (
          <div className="w-full h-full bg-dark-800 flex flex-col items-center justify-center">
            <svg className="w-10 h-10 text-red-500 mb-2" viewBox="0 0 24 24" fill="currentColor">
              <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
            </svg>
            <span className="text-xs text-dark-400 truncate max-w-full px-2">YouTube</span>
          </div>
        ) : (
          <img
            src={getMediaUrl(item.url)}
            alt={item.altText || ''}
            className="w-full h-full object-cover"
          />
        )}
      </div>

      {/* Type badge */}
      <div className="absolute bottom-2 left-2 px-2 py-0.5 rounded text-xs bg-dark-900/70 text-white">
        {item.type}
      </div>
    </div>
  );
}

export default function MediaManager({ postId, media, onMediaChange }: MediaManagerProps) {
  const [youtubeUrl, setYoutubeUrl] = useState('');
  const [isUploading, setIsUploading] = useState(false);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const uploadMutation = useMutation({
    mutationFn: (file: File) => adminMediaApi.upload(file, postId),
    onSuccess: (response) => {
      if (response.data) {
        onMediaChange([...media, response.data]);
        toast.success('Media uploaded');
      }
    },
    onError: () => {
      toast.error('Failed to upload media');
    },
  });

  const youTubeMutation = useMutation({
    mutationFn: (videoUrl: string) => adminMediaApi.createYouTube(videoUrl, postId),
    onSuccess: (response) => {
      if (response.data) {
        onMediaChange([...media, response.data]);
        setYoutubeUrl('');
        toast.success('YouTube video added');
      }
    },
    onError: () => {
      toast.error('Failed to add YouTube video');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminMediaApi.delete(id),
    onSuccess: (_, deletedId) => {
      onMediaChange(media.filter((m) => m.id !== deletedId));
      toast.success('Media deleted');
    },
    onError: () => {
      toast.error('Failed to delete media');
    },
  });

  const reorderMutation = useMutation({
    mutationFn: (items: { id: string; displayOrder: number }[]) =>
      adminMediaApi.reorder(postId, items),
    onError: () => {
      toast.error('Failed to save order');
    },
  });

  const handleFileUpload = useCallback(
    async (files: FileList | null) => {
      if (!files || files.length === 0) return;
      setIsUploading(true);
      for (const file of Array.from(files)) {
        await uploadMutation.mutateAsync(file);
      }
      setIsUploading(false);
    },
    [uploadMutation]
  );

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      handleFileUpload(e.dataTransfer.files);
    },
    [handleFileUpload]
  );

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleYouTubeAdd = () => {
    if (!youtubeUrl.trim()) return;
    youTubeMutation.mutate(youtubeUrl.trim());
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate(id);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = media.findIndex((m) => m.id === active.id);
      const newIndex = media.findIndex((m) => m.id === over.id);
      const newMedia = arrayMove(media, oldIndex, newIndex);

      // Update local state immediately
      onMediaChange(newMedia);

      // Save to backend
      const reorderItems = newMedia.map((m, index) => ({
        id: m.id,
        displayOrder: index,
      }));
      reorderMutation.mutate(reorderItems);
    }
  };

  return (
    <div className="space-y-4">
      {/* Upload area */}
      <div
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        className="border-2 border-dashed border-dark-600 rounded-lg p-8 text-center hover:border-primary-500/50 transition-colors"
      >
        <input
          type="file"
          id="media-upload"
          multiple
          accept="image/*,video/*"
          onChange={(e) => handleFileUpload(e.target.files)}
          className="hidden"
        />
        <label
          htmlFor="media-upload"
          className="cursor-pointer flex flex-col items-center"
        >
          {isUploading ? (
            <div className="animate-spin w-10 h-10 border-2 border-primary-500 border-t-transparent rounded-full" />
          ) : (
            <>
              <Upload className="w-10 h-10 text-dark-400 mb-2" />
              <span className="text-dark-300">
                Drop files here or click to upload
              </span>
              <span className="text-sm text-dark-500 mt-1">
                Images and videos supported
              </span>
            </>
          )}
        </label>
      </div>

      {/* YouTube URL input */}
      <div className="flex gap-2">
        <div className="relative flex-1">
          <Youtube className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-dark-400" />
          <input
            type="text"
            value={youtubeUrl}
            onChange={(e) => setYoutubeUrl(e.target.value)}
            placeholder="Paste YouTube URL..."
            className="input pl-10"
          />
        </div>
        <button
          type="button"
          onClick={handleYouTubeAdd}
          disabled={!youtubeUrl.trim() || youTubeMutation.isPending}
          className="btn-secondary disabled:opacity-50"
        >
          {youTubeMutation.isPending ? 'Adding...' : 'Add'}
        </button>
      </div>

      {/* Media grid with drag and drop */}
      {media.length > 0 && (
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext items={media.map((m) => m.id)} strategy={rectSortingStrategy}>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {media.map((item) => (
                <SortableMediaItem
                  key={item.id}
                  item={item}
                  onDelete={handleDelete}
                />
              ))}
            </div>
          </SortableContext>
        </DndContext>
      )}

      {media.length === 0 && (
        <div className="text-center py-8 text-dark-400">
          <ImageIcon className="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>No media added yet</p>
        </div>
      )}
    </div>
  );
}
