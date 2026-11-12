import { useState, useEffect, useRef } from 'react';
import { useNerdTranslation } from '@/hooks/useNerdTranslation';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { Calendar, Building2, CheckCircle } from 'lucide-react';
import { experiencesApi } from '@/services/api';
import { useLanguage } from '@/contexts/LanguageContext';
import { queryKeys } from '@/lib/queryKeys';
import LoadingSpinner from '@/components/LoadingSpinner';

// Backend sends LocalDate (YYYY-MM-DD); new Date('YYYY-MM-DD') is UTC midnight and shifts the month west of UTC
function parseLocalDate(dateStr: string): Date {
  const [year, month, day] = dateStr.split('-').map(Number);
  return new Date(year, month - 1, day || 1);
}

function formatDate(dateStr: string, locale: string): string {
  const date = parseLocalDate(dateStr);
  return date.toLocaleDateString(locale === 'pl' ? 'pl-PL' : 'en-US', {
    month: 'short',
    year: 'numeric',
  }).toLowerCase();
}

function calculateMonthsDuration(startDate: string, endDate: string | null): number {
  const start = parseLocalDate(startDate);
  const end = endDate ? parseLocalDate(endDate) : new Date();
  const months = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth());
  return Math.max(1, months + 1); // At least 1 month
}

function FatigueCheck({ nerdMode }: { nerdMode: boolean }) {
  const { t } = useNerdTranslation();
  const [timeSpent, setTimeSpent] = useState(0);
  const [atBottom, setAtBottom] = useState(false);
  const [revealed, setRevealed] = useState(false);
  const mountTime = useRef(Date.now());

  useEffect(() => {
    if (!nerdMode) return;
    const interval = setInterval(() => {
      setTimeSpent(Math.floor((Date.now() - mountTime.current) / 1000));
    }, 1000);
    return () => clearInterval(interval);
  }, [nerdMode]);

  useEffect(() => {
    if (!nerdMode) return;
    const onScroll = () => {
      const scrollBottom = window.innerHeight + window.scrollY;
      const docHeight = document.documentElement.scrollHeight;
      setAtBottom(docHeight - scrollBottom < 80);
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, [nerdMode]);

  useEffect(() => {
    if (timeSpent >= 60 && atBottom && nerdMode) {
      setRevealed(true);
    }
  }, [timeSpent, atBottom, nerdMode]);

  if (!revealed) return null;

  return (
    <div className="text-center mt-16 mb-4">
      <p className="fatigue-check text-xs text-dark-600 font-mono italic transition-all duration-700 animate-fade-in">
        <span className="fatigue-default">{t('experience.fatiguePassed')}</span>
        <span className="fatigue-hover">{t('experience.fatigueHover')}</span>
      </p>
    </div>
  );
}

export default function Experience() {
  const { t, nerdMode } = useNerdTranslation();
  const { language } = useLanguage();

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.experiences(language),
    queryFn: () => experiencesApi.getAll(),
  });

  // Sort by start date descending (most recent first)
  const experiences = [...(data?.data || [])].sort((a, b) => {
    return parseLocalDate(b.startDate).getTime() - parseLocalDate(a.startDate).getTime();
  });

  if (isPending) {
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
              <div className="absolute left-8 top-0 bottom-0 w-0.5 bg-gradient-to-b from-primary-500 via-accent to-primary-300" />

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
                            {calculateMonthsDuration(exp.startDate, exp.endDate)} {t('experience.monthsShort')}
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

            {isError && !data && (
              <div className="text-center py-12">
                <p className="text-dark-400 mb-4">{t('common.loadError')}</p>
                <button type="button" onClick={() => refetch()} className="btn-secondary">
                  {t('common.retry')}
                </button>
              </div>
            )}

            {!isError && experiences.length === 0 && (
              <div className="text-center text-dark-400 py-12">
                {t('experience.empty')}
              </div>
            )}
          </div>

          <FatigueCheck nerdMode={nerdMode} />
        </div>
      </section>
    </>
  );
}
