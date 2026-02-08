import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import {
  LayoutDashboard,
  FileText,
  Image,
  Settings,
  LogOut,
  ChevronLeft,
  User,
  Briefcase,
  Lightbulb,
  FolderTree,
  Heart,
  Trophy,
  Dices,
} from 'lucide-react';
import { clsx } from 'clsx';
import { useAuth } from '@/contexts/AuthContext';

const navItems = [
  { path: '/admin', icon: LayoutDashboard, labelKey: 'admin.dashboard.title', exact: true },
  { path: '/admin/posts', icon: FileText, labelKey: 'admin.posts.title' },
  { path: '/admin/experiences', icon: Briefcase, labelKey: 'admin.experiences.title' },
  { path: '/admin/soft-skills', icon: Lightbulb, labelKey: 'admin.softSkills.title' },
  { path: '/admin/skill-categories', icon: FolderTree, labelKey: 'admin.skillCategories.title' },
  { path: '/admin/interests', icon: Heart, labelKey: 'admin.interests.title' },
  { path: '/admin/achievements', icon: Trophy, labelKey: 'admin.achievements.title' },
  { path: '/admin/rpg-stats', icon: Dices, labelKey: 'admin.rpgStats.title' },
  { path: '/admin/media', icon: Image, labelKey: 'admin.media.title' },
  { path: '/admin/settings', icon: Settings, labelKey: 'admin.settings.title' },
];

export default function AdminLayout() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-dark-900 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-dark-800 border-r border-dark-700 flex flex-col">
        {/* Logo */}
        <div className="p-6 border-b border-dark-700">
          <button
            onClick={() => navigate('/')}
            className="flex items-center text-dark-400 hover:text-dark-100 transition-colors"
          >
            <ChevronLeft className="w-5 h-5 mr-2" />
            <span className="text-lg font-display font-bold gradient-text">Admin Panel</span>
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.exact}
              className={({ isActive }) =>
                clsx(
                  'flex items-center px-4 py-3 rounded-lg text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-primary-500/10 text-primary-400'
                    : 'text-dark-400 hover:bg-dark-700 hover:text-dark-100'
                )
              }
            >
              <item.icon className="w-5 h-5 mr-3" />
              {t(item.labelKey)}
            </NavLink>
          ))}
        </nav>

        {/* User Info */}
        <div className="p-4 border-t border-dark-700">
          <div className="flex items-center mb-4">
            {user?.avatarUrl ? (
              <img
                src={user.avatarUrl}
                alt={user.name}
                className="w-10 h-10 rounded-full mr-3"
              />
            ) : (
              <div className="w-10 h-10 rounded-full bg-primary-500/20 flex items-center justify-center mr-3">
                <User className="w-5 h-5 text-primary-400" />
              </div>
            )}
            <div className="flex-1 min-w-0">
              <div className="text-sm font-medium text-dark-100 truncate">{user?.name}</div>
              <div className="text-xs text-dark-500 truncate">{user?.email}</div>
            </div>
          </div>
          <button
            onClick={logout}
            className="flex items-center w-full px-4 py-2 text-sm text-dark-400 hover:text-red-400 hover:bg-dark-700 rounded-lg transition-colors"
          >
            <LogOut className="w-5 h-5 mr-3" />
            {t('admin.auth.logout')}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="p-8"
        >
          <Outlet />
        </motion.div>
      </main>
    </div>
  );
}
