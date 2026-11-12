import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';

export default function NotFound() {
  const { t } = useTranslation();

  return (
    <>
      <Helmet>
        <title>{t('notFound.title')} | Portfolio</title>
        <meta name="robots" content="noindex" />
      </Helmet>

      <section className="py-20">
        <div className="container text-center">
          <h1 className="section-heading">{t('notFound.title')}</h1>
          <p className="section-subheading mx-auto">{t('notFound.message')}</p>
          <Link to="/" className="btn-primary mt-8">
            {t('notFound.backHome')}
          </Link>
        </div>
      </section>
    </>
  );
}
