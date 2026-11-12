package com.portfolio.service;

import com.portfolio.dto.MediaDto;
import com.portfolio.dto.ReorderRequest;
import com.portfolio.entity.Media;
import com.portfolio.entity.MediaType;
import com.portfolio.entity.Post;
import com.portfolio.repository.MediaRepository;
import com.portfolio.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MediaService {

    // Column sizes of the media table (V1__initial_schema.sql, V4__enhance_posts_media.sql)
    private static final int MAX_TEXT_COLUMN_LENGTH = 255;
    private static final int MAX_VIDEO_URL_LENGTH = 500;

    // Share links from the YouTube apps put v= after other parameters (watch?si=..&v=..) and use
    // /shorts/ and /live/; [^#]* keeps the match from reaching into the fragment.
    private static final Pattern YOUTUBE_VIDEO_ID = Pattern.compile(
            "(?:youtube(?:-nocookie)?\\.com/(?:watch\\?(?:[^#]*&)?v=|embed/|shorts/|live/)|youtu\\.be/)"
                    + "([a-zA-Z0-9_-]{11})"
    );

    private final MediaRepository mediaRepository;
    private final PostRepository postRepository;
    private final StorageService storageService;

    public Page<MediaDto> getAllMedia(Pageable pageable, String locale) {
        return mediaRepository.findAll(pageable)
                .map(media -> MediaDto.from(media, locale));
    }

    public Page<MediaDto> getUnassignedMedia(Pageable pageable, String locale) {
        return mediaRepository.findByPostIdIsNull(pageable)
                .map(media -> MediaDto.from(media, locale));
    }

    public Page<MediaDto> getMediaByType(MediaType type, Pageable pageable, String locale) {
        return mediaRepository.findByType(type, pageable)
                .map(media -> MediaDto.from(media, locale));
    }

    @Transactional
    public MediaDto uploadMedia(MultipartFile file, UUID postId, String altTextEn, String altTextPl, String locale) throws IOException {
        // Everything that can reject the upload runs before the file is written, so a rejected
        // request does not leave an orphan file on the volume.
        requireAltTextsFit(altTextEn, altTextPl);

        Post post = null;
        int displayOrder = 0;
        if (postId != null) {
            post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
            displayOrder = mediaRepository.getMaxDisplayOrderForPost(postId) + 1;
        }

        String filename = storageService.store(file);
        MediaType type = storageService.getMediaType(file.getContentType());

        Media media = Media.builder()
                .post(post)
                .type(type)
                .filename(filename)
                .originalName(truncate(file.getOriginalFilename()))
                .mimeType(file.getContentType())
                .size(file.getSize())
                .url("/api/media/" + filename)
                .altTextEn(altTextEn)
                .altTextPl(altTextPl)
                .displayOrder(displayOrder)
                .build();

        try {
            // Flush so a failing INSERT surfaces here, while the stored file can still be removed
            media = mediaRepository.saveAndFlush(media);
        } catch (RuntimeException e) {
            try {
                storageService.delete(filename);
            } catch (IOException | RuntimeException cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }
            throw e;
        }
        return MediaDto.from(media, locale);
    }

    private static void requireAltTextsFit(String altTextEn, String altTextPl) {
        requireMaxLength("altTextEn", altTextEn, MAX_TEXT_COLUMN_LENGTH);
        requireMaxLength("altTextPl", altTextPl, MAX_TEXT_COLUMN_LENGTH);
    }

    private static void requireMaxLength(String field, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
    }

    private static String truncate(String value) {
        return value != null && value.length() > MAX_TEXT_COLUMN_LENGTH
                ? value.substring(0, MAX_TEXT_COLUMN_LENGTH)
                : value;
    }

    @Transactional
    public MediaDto createYouTubeMedia(UUID postId, String videoUrl, String altTextEn, String altTextPl, String locale) {
        requireAltTextsFit(altTextEn, altTextPl);
        requireMaxLength("videoUrl", videoUrl, MAX_VIDEO_URL_LENGTH);

        Post post = null;
        int displayOrder = 0;
        if (postId != null) {
            post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
            displayOrder = mediaRepository.getMaxDisplayOrderForPost(postId) + 1;
        }

        // Extract YouTube video ID and create embed URL
        String videoId = extractYouTubeVideoId(videoUrl);
        String embedUrl = "https://www.youtube.com/embed/" + videoId;

        Media media = Media.builder()
                .post(post)
                .type(MediaType.YOUTUBE)
                .url(embedUrl)
                .videoUrl(videoUrl)
                .altTextEn(altTextEn)
                .altTextPl(altTextPl)
                .displayOrder(displayOrder)
                .build();

        media = mediaRepository.save(media);
        return MediaDto.from(media, locale);
    }

    @Transactional
    public void reorderMedia(UUID postId, ReorderRequest request) {
        // Verify post exists
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found: " + postId);
        }

        for (ReorderRequest.OrderItem item : request.getItems()) {
            mediaRepository.updateDisplayOrder(item.getId(), item.getDisplayOrder());
        }
    }

    private String extractYouTubeVideoId(String url) {
        Matcher matcher = YOUTUBE_VIDEO_ID.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid YouTube URL: " + url);
    }

    @Transactional
    public MediaDto updateMedia(UUID id, String altTextEn, String altTextPl, UUID postId, String locale) {
        requireAltTextsFit(altTextEn, altTextPl);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));

        if (altTextEn != null) {
            media.setAltTextEn(altTextEn);
        }
        if (altTextPl != null) {
            media.setAltTextPl(altTextPl);
        }
        if (postId != null) {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
            media.setPost(post);
        }

        media = mediaRepository.save(media);
        return MediaDto.from(media, locale);
    }

    @Transactional
    public void deleteMedia(UUID id) throws IOException {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));

        mediaRepository.delete(media);
        deleteStoredFileAfterCommit(media.getFilename());
    }

    @Transactional
    public void deleteAllForPost(UUID postId) {
        List<Media> media = mediaRepository.findByPostId(postId);
        mediaRepository.deleteAll(media);
        media.forEach(item -> deleteStoredFileAfterCommit(item.getFilename()));
    }

    private void deleteStoredFileAfterCommit(String filename) {
        // YouTube rows have no stored file
        if (filename == null) {
            return;
        }
        // The file goes only once the row is gone for good; a rollback must not leave a row
        // that points to a deleted file.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteStoredFile(filename);
            }
        });
    }

    private void deleteStoredFile(String filename) {
        try {
            storageService.delete(filename);
        } catch (IOException | RuntimeException e) {
            log.warn("Media row deleted but its file could not be removed: {} ({})", filename, e.getMessage());
        }
    }
}
