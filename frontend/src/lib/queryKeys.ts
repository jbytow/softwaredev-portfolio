import { Category } from '@/types';

export const queryKeys = {
  // Public queries (language-dependent)
  settings: (lang: string) => ['settings', lang] as const,
  posts: (lang: string, category?: Category | 'featured') =>
    ['posts', lang, category] as const,
  postsByHashtag: (lang: string, hashtag: string) =>
    ['posts', lang, 'hashtag', hashtag] as const,
  post: (lang: string, slug: string) => ['post', lang, slug] as const,
  softSkills: (lang: string) => ['softSkills', lang] as const,
  skillCategories: (lang: string) => ['skillCategories', lang] as const,
  skillCategoriesWithSkills: (lang: string) => ['skillCategoriesWithSkills', lang] as const,
  experiences: (lang: string) => ['experiences', lang] as const,
  interests: (lang: string) => ['interests', lang] as const,
  hashtags: () => ['hashtags'] as const,

  // Admin queries (language-independent, manages all languages)
  admin: {
    posts: (category?: Category) => ['admin', 'posts', category] as const,
    post: (id: string) => ['admin', 'post', id] as const,
    settings: () => ['admin', 'settings'] as const,
    media: () => ['admin', 'media'] as const,
    softSkills: () => ['admin', 'softSkills'] as const,
    softSkill: (id: string) => ['admin', 'softSkill', id] as const,
    skillCategories: () => ['admin', 'skillCategories'] as const,
    skillCategory: (id: string) => ['admin', 'skillCategory', id] as const,
    experiences: () => ['admin', 'experiences'] as const,
    experience: (id: string) => ['admin', 'experience', id] as const,
    interests: () => ['admin', 'interests'] as const,
    interest: (id: string) => ['admin', 'interest', id] as const,
  },
};
