import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import {
  Lightbulb,
  Users,
  MessageSquare,
  Target,
  Clock,
  Heart,
  Zap,
  Brain,
  Handshake,
  Rocket,
  Sparkles,
  Award,
  Code,
  Terminal,
  Database,
  Server,
  GitBranch,
  Monitor,
  Cloud,
  Lock,
} from 'lucide-react';
import { skillCategoriesApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import { SkillCategoryWithSkills } from '@/types';
import LoadingSpinner from '@/components/LoadingSpinner';

const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
  lightbulb: Lightbulb,
  users: Users,
  message: MessageSquare,
  target: Target,
  clock: Clock,
  heart: Heart,
  zap: Zap,
  brain: Brain,
  handshake: Handshake,
  rocket: Rocket,
  sparkles: Sparkles,
  award: Award,
  code: Code,
  terminal: Terminal,
  database: Database,
  server: Server,
  'git-branch': GitBranch,
  monitor: Monitor,
  cloud: Cloud,
  lock: Lock,
};

const defaultIcons = [Code, Terminal, Database, Monitor, GitBranch, Server];

export default function Skills() {
  const { t } = useTranslation();
  const { language } = useLanguage();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.skillCategoriesWithSkills(language),
    queryFn: () => skillCategoriesApi.getAllWithSkills(),
  });

  const categories: SkillCategoryWithSkills[] = data?.data || [];

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  const getIcon = (iconName: string | null, index: number) => {
    if (iconName && iconMap[iconName.toLowerCase()]) {
      return iconMap[iconName.toLowerCase()];
    }
    return defaultIcons[index % defaultIcons.length];
  };

  return (
    <>
      <Helmet>
        <title>{t('skills.title')} | Portfolio</title>
      </Helmet>

      <section className="py-20">
        <div className="container">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-16"
          >
            <h1 className="section-heading">{t('skills.title')}</h1>
            <p className="section-subheading mx-auto">{t('skills.subtitle')}</p>
          </motion.div>

          {categories.filter(cat => cat.skills.length > 0).map((category, categoryIndex) => (
            <motion.div
              key={category.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: categoryIndex * 0.15 }}
              className="mb-12"
            >
              <h2 className="text-2xl font-display font-bold text-dark-100 mb-6 pb-2 border-b border-dark-700">
                {category.name}
              </h2>
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
                {category.skills.map((skill, skillIndex) => {
                  const IconComponent = getIcon(skill.icon, skillIndex);
                  return (
                    <motion.div
                      key={skill.id}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: categoryIndex * 0.15 + skillIndex * 0.05 }}
                      className="card-hover text-center"
                    >
                      <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary-500/20 to-accent-lime/20 mb-6">
                        <IconComponent className="w-8 h-8 text-primary-400" />
                      </div>

                      <h3 className="text-xl font-semibold text-dark-100 mb-3">{skill.name}</h3>

                      <p className="text-dark-400 mb-4">{skill.description}</p>

                      {skill.professionalUsage && (
                        <p className="text-dark-500 text-sm italic">{skill.professionalUsage}</p>
                      )}
                    </motion.div>
                  );
                })}
              </div>
            </motion.div>
          ))}

          {categories.every(cat => cat.skills.length === 0) && (
            <div className="text-center">
              {/* Default skills when no skills exist */}
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[
                  { icon: Code, title: 'Java', desc: 'Core language for backend development' },
                  { icon: Terminal, title: 'Spring Boot', desc: 'Building robust REST APIs and microservices' },
                  { icon: Database, title: 'PostgreSQL', desc: 'Relational database design and optimization' },
                  { icon: Monitor, title: 'React', desc: 'Building modern, responsive user interfaces' },
                  { icon: GitBranch, title: 'Git', desc: 'Version control and collaborative workflows' },
                  { icon: Server, title: 'Docker', desc: 'Containerization and deployment' },
                ].map((skill, index) => (
                  <motion.div
                    key={skill.title}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.1 }}
                    className="card-hover text-center"
                  >
                    <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary-500/20 to-accent-lime/20 mb-6">
                      <skill.icon className="w-8 h-8 text-primary-400" />
                    </div>
                    <h3 className="text-xl font-semibold text-dark-100 mb-3">{skill.title}</h3>
                    <p className="text-dark-400">{skill.desc}</p>
                  </motion.div>
                ))}
              </div>
            </div>
          )}
        </div>
      </section>
    </>
  );
}
