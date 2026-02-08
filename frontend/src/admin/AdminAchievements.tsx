import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { Plus, Edit2, Trash2, GripVertical, Trophy } from 'lucide-react';
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
import { adminAchievementsApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import { Achievement } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';

function SortableAchievementItem({
  achievement,
  onDelete,
}: {
  achievement: Achievement;
  onDelete: (id: string) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({
    id: achievement.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

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

      <div className="w-10 h-10 rounded-lg bg-primary-500/20 flex items-center justify-center flex-shrink-0">
        <Trophy className="w-5 h-5 text-primary-400" />
      </div>

      <div className="flex-1 min-w-0">
        <h3 className="font-medium text-dark-100 truncate">{achievement.titleEn}</h3>
        <p className="text-sm text-dark-400 truncate">{achievement.titlePl}</p>
      </div>

      {achievement.year && (
        <span className="text-xs font-mono text-primary-400/70 flex-shrink-0">
          {achievement.year}
        </span>
      )}

      <div className="flex items-center gap-2">
        <Link
          to={`/admin/achievements/${achievement.id}/edit`}
          className="p-2 text-dark-400 hover:text-primary-400 transition-colors"
        >
          <Edit2 className="w-4 h-4" />
        </Link>
        <button
          onClick={() => onDelete(achievement.id)}
          className="p-2 text-dark-400 hover:text-red-400 transition-colors"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}

export default function AdminAchievements() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [achievements, setAchievements] = useState<Achievement[]>([]);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.achievements(),
    queryFn: () => adminAchievementsApi.getAll(),
  });

  useEffect(() => {
    if (data?.data) {
      setAchievements(data.data);
    }
  }, [data]);

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminAchievementsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.achievements() });
      toast.success('Achievement deleted');
    },
    onError: () => {
      toast.error('Failed to delete achievement');
    },
  });

  const reorderMutation = useMutation({
    mutationFn: (items: { id: string; displayOrder: number }[]) =>
      adminAchievementsApi.reorder(items),
    onError: () => {
      toast.error('Failed to reorder');
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.achievements() });
    },
  });

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (over && active.id !== over.id) {
      const oldIndex = achievements.findIndex((i) => i.id === active.id);
      const newIndex = achievements.findIndex((i) => i.id === over.id);
      const newItems = arrayMove(achievements, oldIndex, newIndex);
      setAchievements(newItems);

      const reorderItems = newItems.map((item, index) => ({
        id: item.id,
        displayOrder: index,
      }));
      reorderMutation.mutate(reorderItems);
    }
  };

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this achievement?')) {
      deleteMutation.mutate(id);
    }
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display font-bold text-dark-100">
            {t('admin.achievements.title')}
          </h1>
          <p className="text-dark-400 mt-1">{t('admin.achievements.subtitle')}</p>
        </div>
        <Link to="/admin/achievements/new" className="btn-primary">
          <Plus className="w-5 h-5 mr-2" />
          {t('admin.achievements.newAchievement')}
        </Link>
      </div>

      {achievements.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-dark-400 mb-4">{t('admin.achievements.noAchievementsYet')}</p>
          <Link to="/admin/achievements/new" className="btn-primary">
            {t('admin.achievements.createFirst')}
          </Link>
        </div>
      ) : (
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext items={achievements} strategy={verticalListSortingStrategy}>
            <div className="space-y-2">
              {achievements.map((achievement) => (
                <SortableAchievementItem
                  key={achievement.id}
                  achievement={achievement}
                  onDelete={handleDelete}
                />
              ))}
            </div>
          </SortableContext>
        </DndContext>
      )}
    </div>
  );
}
