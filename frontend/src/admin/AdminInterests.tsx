import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { Plus, Edit2, Trash2, GripVertical } from 'lucide-react';
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
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { adminInterestsApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import { Interest } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import { getMediaUrl } from '@/lib/mediaUrl';
import { getApiErrorMessage } from '@/lib/apiError';
import ConfirmDeleteModal from '@/admin/components/ConfirmDeleteModal';

function SortableInterestItem({
  interest,
  onDelete,
}: {
  interest: Interest;
  onDelete: (id: string) => void;
}) {
  const { t } = useTranslation();

  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({
    id: interest.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  const images = [interest.image1, interest.image2, interest.image3].filter(Boolean);

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="bg-dark-700 rounded-lg p-4 flex items-center gap-4"
    >
      <button
        {...attributes}
        {...listeners}
        className="cursor-grab text-dark-500 hover:text-dark-300"
      >
        <GripVertical className="w-5 h-5" />
      </button>

      <div className="flex gap-2">
        {images.slice(0, 3).map((img, idx) => (
          <img
            key={idx}
            src={getMediaUrl(img!)}
            alt=""
            className="w-12 h-12 rounded object-cover"
          />
        ))}
        {images.length === 0 && (
          <div className="w-12 h-12 rounded bg-dark-600 flex items-center justify-center text-dark-400 text-xs">
            {t('admin.interests.noImage')}
          </div>
        )}
      </div>

      <div className="flex-1 min-w-0">
        <h3 className="font-medium text-dark-100 truncate">{interest.titleEn}</h3>
        <p className="text-sm text-dark-400 truncate">{interest.titlePl}</p>
      </div>

      <div className="flex items-center gap-2">
        <Link
          to={`/admin/interests/${interest.id}/edit`}
          className="p-2 text-dark-400 hover:text-primary-400 transition-colors"
        >
          <Edit2 className="w-4 h-4" />
        </Link>
        <button
          onClick={() => onDelete(interest.id)}
          className="p-2 text-dark-400 hover:text-red-400 transition-colors"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}

export default function AdminInterests() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [deleteTarget, setDeleteTarget] = useState<{ id: string; message: string } | null>(null);
  const [interests, setInterests] = useState<Interest[]>([]);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.interests(),
    queryFn: () => adminInterestsApi.getAll(),
  });

  // Update local state when data changes
  useEffect(() => {
    if (data?.data) {
      setInterests(data.data);
    }
  }, [data]);

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminInterestsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.interests() });
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.interests() });
      setDeleteTarget(null);
      toast.success(t('admin.interests.deleted'));
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.interests.deleteFailed')));
    },
  });

  const reorderMutation = useMutation({
    mutationFn: (items: { id: string; displayOrder: number }[]) =>
      adminInterestsApi.reorder(items),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.prefix.interests() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, t('admin.interests.reorderFailed')));
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.interests() });
    },
  });

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (over && active.id !== over.id) {
      const oldIndex = interests.findIndex((i) => i.id === active.id);
      const newIndex = interests.findIndex((i) => i.id === over.id);
      const previous = interests;
      const newItems = arrayMove(interests, oldIndex, newIndex);
      setInterests(newItems);

      const reorderItems = newItems.map((item, index) => ({
        id: item.id,
        displayOrder: index,
      }));
      reorderMutation.mutate(reorderItems, { onError: () => setInterests(previous) });
    }
  };

  const handleDelete = (id: string) => {
    setDeleteTarget({ id, message: t('admin.interests.confirmDeleteMessage') });
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display font-bold text-dark-100">
            {t('admin.interests.title')}
          </h1>
          <p className="text-dark-400 mt-1">{t('admin.interests.subtitle')}</p>
        </div>
        <Link to="/admin/interests/new" className="btn-primary">
          <Plus className="w-5 h-5 mr-2" />
          {t('admin.interests.newInterest')}
        </Link>
      </div>

      {interests.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-dark-400 mb-4">{t('admin.interests.noInterestsYet')}</p>
          <Link to="/admin/interests/new" className="btn-primary">
            {t('admin.interests.createFirst')}
          </Link>
        </div>
      ) : (
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext items={interests} strategy={verticalListSortingStrategy}>
            <div className="space-y-2">
              {interests.map((interest) => (
                <SortableInterestItem
                  key={interest.id}
                  interest={interest}
                  onDelete={handleDelete}
                />
              ))}
            </div>
          </SortableContext>
        </DndContext>
      )}

      <ConfirmDeleteModal
        open={deleteTarget !== null}
        title={t('admin.common.confirmDelete')}
        message={deleteTarget?.message ?? ''}
        isPending={deleteMutation.isPending}
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
