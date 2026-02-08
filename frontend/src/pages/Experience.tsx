import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { Calendar, Building2, CheckCircle } from 'lucide-react';
import { experiencesApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

function formatDate(dateStr: string, locale: string): string {
  const date = new Date(dateStr);
  return date.toLocaleDateString(locale === 'pl' ? 'pl-PL' : 'en-US', {
    month: 'short',
    year: 'numeric',
  }).toLowerCase();
}

function calculateMonthsDuration(startDate: string, endDate: string | null): number {
  const start = new Date(startDate);
  const end = endDate ? new Date(endDate) : new Date();
  const months = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth());
  return Math.max(1, months + 1); // At least 1 month
}

export default function Experience() {
  const { t } = useTranslation();
  const { language } = useLanguage();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.experiences(language),
    queryFn: () => experiencesApi.getAll(),
  });

  // Sort by start date descending (most recent first)
  const experiences = [...(data?.data || [])].sort((a, b) => {
    return new Date(b.startDate).getTime() - new Date(a.startDate).getTime();
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <>
      <Helmet>
        <title>{t('experience.title')} | Portfolio</title>
      </Helmet>

      <section className="py-20">
        <div className="container">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-16"
          >
            <h1 className="section-heading">{t('experience.title')}</h1>
            <p className="section-subheading mx-auto">{t('experience.subtitle')}</p>
          </motion.div>

          <div className="max-w-3xl mx-auto">
            <div className="relative">
              {/* Timeline line */}
              <div className="absolute left-8 top-0 bottom-0 w-0.5 bg-gradient-to-b from-primary-500 via-accent-teal to-primary-300" />

              <div className="space-y-8">
                {experiences.map((exp, index) => (
                  <motion.div
                    key={exp.id}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: index * 0.1 }}
                    className="relative pl-20"
                  >
                    {/* Timeline dot */}
                    <div className="absolute left-6 top-6 w-5 h-5 rounded-full bg-primary-500 border-4 border-dark-900 z-10" />

                    <div className="card">
                      <div className="flex items-start justify-between mb-4">
                        <div>
                          <h3 className="text-xl font-semibold text-dark-100">{exp.title}</h3>
                          <div className="flex items-center text-dark-400 mt-1">
                            <Building2 className="w-4 h-4 mr-2" />
                            <span>{exp.company}</span>
                          </div>
                        </div>
                        <div className="text-right flex-shrink-0">
                          <div className="flex items-center text-dark-300 text-sm whitespace-nowrap">
                            <Calendar className="w-4 h-4 mr-2 flex-shrink-0" />
                            <span>
                              {formatDate(exp.startDate, language)} – {exp.endDate ? formatDate(exp.endDate, language) : t('experience.present')}
                            </span>
                          </div>
                          <div className="text-dark-500 text-xs mt-1">
                            {calculateMonthsDuration(exp.startDate, exp.endDate)} {language === 'pl' ? 'mies.' : 'mo.'}
                          </div>
                        </div>
                      </div>

                      {exp.description && (
                        <p className="text-dark-300 mb-4">{exp.description}</p>
                      )}

                      {exp.achievements && exp.achievements.length > 0 && (
                        <ul className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-2">
                          {exp.achievements.map((achievement, i) => (
                            <li key={i} className="flex items-start text-dark-400 text-sm">
                              <CheckCircle className="w-4 h-4 mr-2 mt-0.5 text-primary-400 flex-shrink-0" />
                              <span>{achievement}</span>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>

            {experiences.length === 0 && (
              <div className="text-center text-dark-400 py-12">
                No experience entries yet.
              </div>
            )}
          </div>
        </div>
      </section>
    </>
  );
}
