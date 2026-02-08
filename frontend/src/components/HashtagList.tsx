import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronDown, ChevronUp } from 'lucide-react';

interface HashtagListProps {
  hashtags: string[];
  clickable?: boolean;
  limit?: number;
  expandable?: boolean;
  className?: string;
}

export default function HashtagList({
  hashtags,
  clickable = true,
  limit,
  expandable = false,
  className = '',
}: HashtagListProps) {
  const [isExpanded, setIsExpanded] = useState(false);

  if (!hashtags || hashtags.length === 0) {
    return null;
  }

  const shouldLimit = limit && hashtags.length > limit && !isExpanded;
  const displayHashtags = shouldLimit ? hashtags.slice(0, limit) : hashtags;
  const remainingCount = limit && hashtags.length > limit ? hashtags.length - limit : 0;
  const canExpand = expandable && remainingCount > 0;

  const ExpandCollapseButton = () => {
    if (!canExpand) {
      if (remainingCount > 0) {
        return (
          <span className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-dark-700 text-dark-400">
            +{remainingCount}
          </span>
        );
      }
      return null;
    }

    if (isExpanded) {
      return (
        <button
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            setIsExpanded(false);
          }}
          className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm bg-dark-700 text-dark-400 hover:bg-dark-600 hover:text-dark-300 transition-colors"
        >
          <ChevronUp className="w-3 h-3" />
          Less
        </button>
      );
    }

    return (
      <button
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          setIsExpanded(true);
        }}
        className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm bg-dark-700 text-dark-400 hover:bg-dark-600 hover:text-dark-300 transition-colors"
      >
        +{remainingCount}
        <ChevronDown className="w-3 h-3" />
      </button>
    );
  };

  if (clickable) {
    return (
      <div className={`flex flex-wrap gap-2 ${className}`}>
        {displayHashtags.map((hashtag) => (
          <Link
            key={hashtag}
            to={`/projects?hashtag=${encodeURIComponent(hashtag)}`}
            className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-primary-500/10 text-primary-400 hover:bg-primary-500/20 transition-colors"
          >
            #{hashtag}
          </Link>
        ))}
        <ExpandCollapseButton />
      </div>
    );
  }

  return (
    <div className={`flex flex-wrap gap-2 ${className}`}>
      {displayHashtags.map((hashtag) => (
        <span
          key={hashtag}
          className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-dark-700 text-dark-300"
        >
          #{hashtag}
        </span>
      ))}
      <ExpandCollapseButton />
    </div>
  );
}
