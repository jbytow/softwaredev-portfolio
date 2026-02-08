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
import { adminRpgStatsApi } from '@/services/api';
import { queryKeys } from '@/lib/queryKeys';
import { RpgStat } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';

function SortableRpgStatItem({
  stat,
  onDelete,
}: {
  stat: RpgStat;
  onDelete: (id: string) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({
    id: stat.id,
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

      <div className="w-12 h-8 rounded bg-primary-500/20 flex items-center justify-center flex-shrink-0">
        <span className="text-sm font-mono font-bold text-primary-400">{stat.attr}</span>
      </div>

      <div className="flex-1 min-w-0">
        <h3 className="font-medium text-dark-100 truncate">{stat.labelEn}</h3>
        <p className="text-sm text-dark-400 truncate">{stat.labelPl}</p>
      </div>

      <span className="text-sm font-mono text-dark-400 flex-shrink-0">
        {stat.level}/{stat.maxLevel}
      </span>

      <div className="flex items-center gap-2">
        <Link
          to={`/admin/rpg-stats/${stat.id}/edit`}
          className="p-2 text-dark-400 hover:text-primary-400 transition-colors"
        >
          <Edit2 className="w-4 h-4" />
        </Link>
        <button
          onClick={() => onDelete(stat.id)}
          className="p-2 text-dark-400 hover:text-red-400 transition-colors"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}

export default function AdminRpgStats() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [stats, setStats] = useState<RpgStat[]>([]);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.admin.rpgStats(),
    queryFn: () => adminRpgStatsApi.getAll(),
  });

  useEffect(() => {
    if (data?.data) {
      setStats(data.data);
    }
  }, [data]);

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminRpgStatsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.rpgStats() });
      toast.success('RPG stat deleted');
    },
    onError: () => {
      toast.error('Failed to delete RPG stat');
    },
  });

  const reorderMutation = useMutation({
    mutationFn: (items: { id: string; displayOrder: number }[]) =>
      adminRpgStatsApi.reorder(items),
    onError: () => {
      toast.error('Failed to reorder');
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.rpgStats() });
    },
  });

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (over && active.id !== over.id) {
      const oldIndex = stats.findIndex((i) => i.id === active.id);
      const newIndex = stats.findIndex((i) => i.id === over.id);
      const newItems = arrayMove(stats, oldIndex, newIndex);
      setStats(newItems);

      const reorderItems = newItems.map((item, index) => ({
        id: item.id,
        displayOrder: index,
      }));
      reorderMutation.mutate(reorderItems);
    }
  };

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this RPG stat?')) {
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
            {t('admin.rpgStats.title')}
          </h1>
          <p className="text-dark-400 mt-1">{t('admin.rpgStats.subtitle')}</p>
        </div>
        <Link to="/admin/rpg-stats/new" className="btn-primary">
          <Plus className="w-5 h-5 mr-2" />
          {t('admin.rpgStats.newStat')}
        </Link>
      </div>

      {stats.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-dark-400 mb-4">{t('admin.rpgStats.noStatsYet')}</p>
          <Link to="/admin/rpg-stats/new" className="btn-primary">
            {t('admin.rpgStats.createFirst')}
          </Link>
        </div>
      ) : (
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext items={stats} strategy={verticalListSortingStrategy}>
            <div className="space-y-2">
              {stats.map((stat) => (
                <SortableRpgStatItem
                  key={stat.id}
                  stat={stat}
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
