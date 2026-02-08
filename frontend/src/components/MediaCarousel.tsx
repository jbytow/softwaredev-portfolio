import { useState } from 'react';
import { motion } from 'framer-motion';
import { Media, MediaType } from '@/types';
import { getMediaUrl } from '@/lib/mediaUrl';

interface MediaCarouselProps {
  media: Media[];
  className?: string;
}

export default function MediaCarousel({ media, className = '' }: MediaCarouselProps) {
  const [currentIndex, setCurrentIndex] = useState(0);

  if (!media || media.length === 0) {
    return null;
  }

  const goToPrevious = () => {
    setCurrentIndex((prev) => (prev === 0 ? media.length - 1 : prev - 1));
  };

  const goToNext = () => {
    setCurrentIndex((prev) => (prev === media.length - 1 ? 0 : prev + 1));
  };

  const getAdjacentIndex = (offset: number) => {
    const newIndex = currentIndex + offset;
    if (newIndex < 0) return media.length + newIndex;
    if (newIndex >= media.length) return newIndex - media.length;
    return newIndex;
  };

  const renderMedia = (item: Media, isMain: boolean = true) => {
    if (item.type === MediaType.YOUTUBE) {
      if (!isMain) {
        // Show thumbnail for side items
        return (
          <div className="w-full h-full bg-dark-700 flex items-center justify-center">
            <svg className="w-12 h-12 text-red-500" viewBox="0 0 24 24" fill="currentColor">
              <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
            </svg>
          </div>
        );
      }
      return (
        <iframe
          src={item.url}
          title={item.altText || 'YouTube video'}
          className="w-full h-full"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowFullScreen
        />
      );
    }

    if (item.type === MediaType.VIDEO) {
      if (!isMain) {
        return (
          <div className="w-full h-full bg-dark-700 flex items-center justify-center">
            <svg className="w-12 h-12 text-dark-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        );
      }
      return (
        <video
          src={getMediaUrl(item.url)}
          controls
          className="w-full h-full object-contain"
        >
          Your browser does not support the video tag.
        </video>
      );
    }

    return (
      <img
        src={getMediaUrl(item.url)}
        alt={item.altText || ''}
        className="w-full h-full object-cover"
      />
    );
  };

  const hasMultiple = media.length > 1;
  const prevIndex = getAdjacentIndex(-1);
  const nextIndex = getAdjacentIndex(1);

  return (
    <div className={`relative ${className}`}>
      {/* Carousel container with overlapping effect */}
      <div className="relative flex items-center justify-center">
        {/* Previous item - behind main, 80% size, half visible */}
        {hasMultiple && (
          <button
            onClick={goToPrevious}
            className="absolute z-0 opacity-70 hover:opacity-90 transition-opacity rounded-xl overflow-hidden"
            style={{
              width: '45%',
              maxWidth: '260px',
              left: '50%',
              transform: 'translateX(-105%) scale(0.8)',
            }}
            aria-label="Previous"
          >
            <div className="w-full bg-dark-800 rounded-xl overflow-hidden" style={{ aspectRatio: '4/5' }}>
              {renderMedia(media[prevIndex], false)}
            </div>
          </button>
        )}

        {/* Main carousel item */}
        <div
          className="relative bg-dark-800 rounded-xl overflow-hidden z-10 max-w-xs w-full shadow-2xl"
          style={{
            aspectRatio: '4/5',
            maxHeight: '50vh',
            boxShadow: '-20px 0 40px rgba(0,0,0,0.5), 20px 0 40px rgba(0,0,0,0.5)'
          }}
        >
          <motion.div
            key={currentIndex}
            initial={{ opacity: 0.5 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.2 }}
            className="w-full h-full"
          >
            {renderMedia(media[currentIndex], true)}
          </motion.div>
        </div>

        {/* Next item - behind main, 80% size, half visible */}
        {hasMultiple && (
          <button
            onClick={goToNext}
            className="absolute z-0 opacity-70 hover:opacity-90 transition-opacity rounded-xl overflow-hidden"
            style={{
              width: '45%',
              maxWidth: '260px',
              right: '50%',
              transform: 'translateX(105%) scale(0.8)',
            }}
            aria-label="Next"
          >
            <div className="w-full bg-dark-800 rounded-xl overflow-hidden" style={{ aspectRatio: '4/5' }}>
              {renderMedia(media[nextIndex], false)}
            </div>
          </button>
        )}
      </div>

      {/* Dots indicator - only show if multiple items */}
      {hasMultiple && (
        <div className="flex justify-center gap-2 mt-4">
          {media.map((_, index) => (
            <button
              key={index}
              onClick={() => setCurrentIndex(index)}
              className={`h-2 rounded-full transition-all ${
                index === currentIndex
                  ? 'bg-primary-500 w-6'
                  : 'bg-dark-600 w-2 hover:bg-dark-500'
              }`}
              aria-label={`Go to slide ${index + 1}`}
            />
          ))}
        </div>
      )}
    </div>
  );
}
