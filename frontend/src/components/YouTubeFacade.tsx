import { useState } from 'react';
import { Play } from 'lucide-react';
import { useTranslation } from 'react-i18next';

const YOUTUBE_ID = /(?:youtube(?:-nocookie)?\.com\/(?:watch\?(?:[^#]*&)?v=|embed\/)|youtu\.be\/)([a-zA-Z0-9_-]{11})/;

// The player is loaded only after a click: an embedded iframe contacts Google for every visitor who merely opens a post
export default function YouTubeFacade({ url, title }: { url: string; title: string }) {
  const { t } = useTranslation();
  const [playing, setPlaying] = useState(false);

  // The stored URL is validated server-side, so anything without a readable id is not rendered at all
  const videoId = url.match(YOUTUBE_ID)?.[1];
  if (!videoId) return null;

  if (playing) {
    return (
      <iframe
        src={`https://www.youtube-nocookie.com/embed/${videoId}?autoplay=1`}
        title={title}
        className="w-full h-full"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowFullScreen
      />
    );
  }

  return (
    <button
      type="button"
      onClick={() => setPlaying(true)}
      aria-label={t('media.playVideo')}
      className="relative w-full h-full bg-dark-700 group"
    >
      <img
        src={`https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`}
        alt=""
        loading="lazy"
        className="w-full h-full object-cover"
      />
      <span className="absolute inset-0 flex items-center justify-center bg-black/20 group-hover:bg-black/30 transition-colors">
        <span className="flex items-center justify-center w-16 h-16 rounded-full bg-red-600 text-white shadow-lg">
          <Play className="w-8 h-8 ml-1" fill="currentColor" aria-hidden="true" />
        </span>
      </span>
    </button>
  );
}
