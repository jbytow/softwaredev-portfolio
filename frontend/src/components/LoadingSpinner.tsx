import { clsx } from 'clsx';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  fullScreen?: boolean;
}

export default function LoadingSpinner({ size = 'md', fullScreen = false }: LoadingSpinnerProps) {
  const sizeConfig = {
    sm: { cup: 'w-8 h-10', steam: 'h-3', handle: 'w-2 h-4' },
    md: { cup: 'w-12 h-14', steam: 'h-5', handle: 'w-3 h-6' },
    lg: { cup: 'w-16 h-20', steam: 'h-7', handle: 'w-4 h-8' },
  };

  const config = sizeConfig[size];

  const coffee = (
    <div className="flex flex-col items-center">
      {/* Steam */}
      <div className={clsx('flex items-end justify-center gap-1 mb-1', config.steam)}>
        <div className="coffee-steam" style={{ animationDelay: '0s' }} />
        <div className="coffee-steam" style={{ animationDelay: '0.4s' }} />
        <div className="coffee-steam" style={{ animationDelay: '0.8s' }} />
      </div>
      {/* Cup */}
      <div className="flex items-start">
        <div className={clsx('coffee-cup relative', config.cup)}>
          <div className="coffee-fill" />
        </div>
        {/* Handle */}
        <div className={clsx('coffee-handle', config.handle)} />
      </div>
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-dark-900 z-50">
        {coffee}
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center p-8">
      {coffee}
    </div>
  );
}
