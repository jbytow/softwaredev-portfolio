package com.portfolio.service;

import com.portfolio.dto.MediaDto;
import com.portfolio.entity.Media;
import com.portfolio.entity.MediaType;
import com.portfolio.entity.Post;
import com.portfolio.repository.MediaRepository;
import com.portfolio.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MediaService")
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MediaService mediaService;

    @Captor
    private ArgumentCaptor<Media> mediaCaptor;

    private static Media youTubeMedia(UUID id) {
        return Media.builder()
                .id(id)
                .type(MediaType.YOUTUBE)
                .url("https://www.youtube.com/embed/dQw4w9WgXcQ")
                .videoUrl("https://youtu.be/dQw4w9WgXcQ")
                .build();
    }

    private static Media imageMedia(UUID id) {
        return Media.builder()
                .id(id)
                .type(MediaType.IMAGE)
                .filename("images/" + id + ".jpg")
                .originalName("photo.jpg")
                .mimeType("image/jpeg")
                .size(10L)
                .url("/api/media/images/" + id + ".jpg")
                .build();
    }

    @Nested
    @DisplayName("uploadMedia")
    class UploadMedia {

        private final MockMultipartFile file =
                new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        private void storeSucceeds(MockMultipartFile upload) throws IOException {
            given(storageService.store(upload)).willReturn("images/stored.jpg");
            given(storageService.getMediaType("image/jpeg")).willReturn(MediaType.IMAGE);
        }

        @Test
        @DisplayName("unknown postId -> EntityNotFoundException before anything is stored")
        void unknownPostStoresNothing() throws Exception {
            UUID postId = UUID.randomUUID();
            given(postRepository.findById(postId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> mediaService.uploadMedia(file, postId, "alt", "alt", "en"))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(storageService, never()).store(any());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("alt text over 255 chars -> IllegalArgumentException before anything is stored")
        void tooLongAltTextStoresNothing(boolean english) throws Exception {
            String tooLong = "a".repeat(256);

            assertThatThrownBy(() -> mediaService.uploadMedia(file, null,
                    english ? tooLong : "ok", english ? "ok" : tooLong, "en"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(storageService, never()).store(any());
        }

        @Test
        @DisplayName("alt text of exactly 255 chars is accepted")
        void maxLengthAltTextIsAccepted() throws Exception {
            String max = "a".repeat(255);
            storeSucceeds(file);
            given(mediaRepository.saveAndFlush(any(Media.class))).willAnswer(invocation -> invocation.getArgument(0));

            MediaDto result = mediaService.uploadMedia(file, null, max, max, "en");

            assertThat(result.getAltTextEn()).hasSize(255);
            assertThat(result.getFilename()).isEqualTo("images/stored.jpg");
        }

        @Test
        @DisplayName("original name longer than 255 chars is truncated to fit the column")
        void longOriginalNameIsTruncated() throws Exception {
            MockMultipartFile longName = new MockMultipartFile(
                    "file", "n".repeat(300) + ".jpg", "image/jpeg", new byte[]{1});
            storeSucceeds(longName);
            given(mediaRepository.saveAndFlush(any(Media.class))).willAnswer(invocation -> invocation.getArgument(0));

            mediaService.uploadMedia(longName, null, null, null, "en");

            verify(mediaRepository).saveAndFlush(mediaCaptor.capture());
            assertThat(mediaCaptor.getValue().getOriginalName()).hasSize(255);
        }

        @Test
        @DisplayName("failed insert removes the stored file and rethrows")
        void failedInsertRemovesStoredFile() throws Exception {
            storeSucceeds(file);
            DataIntegrityViolationException failure = new DataIntegrityViolationException("value too long");
            given(mediaRepository.saveAndFlush(any(Media.class))).willThrow(failure);

            assertThatThrownBy(() -> mediaService.uploadMedia(file, null, null, null, "en"))
                    .isSameAs(failure);

            verify(storageService).delete("images/stored.jpg");
        }

        @Test
        @DisplayName("failed cleanup does not hide the insert failure")
        void failedCleanupKeepsOriginalException() throws Exception {
            storeSucceeds(file);
            DataIntegrityViolationException failure = new DataIntegrityViolationException("value too long");
            given(mediaRepository.saveAndFlush(any(Media.class))).willThrow(failure);
            willThrow(new IOException("disk gone")).given(storageService).delete("images/stored.jpg");

            assertThatThrownBy(() -> mediaService.uploadMedia(file, null, null, null, "en"))
                    .isSameAs(failure)
                    .satisfies(thrown -> assertThat(thrown.getSuppressed()).hasSize(1));
        }
    }

    @Nested
    @DisplayName("createYouTubeMedia")
    class CreateYouTubeMedia {

        @ParameterizedTest
        @ValueSource(strings = {
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "https://www.youtube.com/watch?si=AbC-123_xyz&v=dQw4w9WgXcQ",
                "https://www.youtube.com/watch?feature=share&t=10&v=dQw4w9WgXcQ&list=PL1",
                "https://www.youtube.com/shorts/dQw4w9WgXcQ",
                "https://youtube.com/shorts/dQw4w9WgXcQ?si=abc",
                "https://www.youtube.com/live/dQw4w9WgXcQ?si=abc",
                "https://www.youtube.com/embed/dQw4w9WgXcQ",
                "https://youtu.be/dQw4w9WgXcQ",
                "https://youtu.be/dQw4w9WgXcQ?t=42",
                "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
                "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
                "youtube.com/watch?v=dQw4w9WgXcQ"
        })
        @DisplayName("accepts every YouTube link format and stores the embed URL")
        void extractsVideoId(String videoUrl) {
            given(mediaRepository.save(any(Media.class))).willAnswer(invocation -> invocation.getArgument(0));

            MediaDto result = mediaService.createYouTubeMedia(null, videoUrl, null, null, "en");

            assertThat(result.getUrl()).isEqualTo("https://www.youtube.com/embed/dQw4w9WgXcQ");
            assertThat(result.getVideoUrl()).isEqualTo(videoUrl);
            assertThat(result.getType()).isEqualTo(MediaType.YOUTUBE);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https://vimeo.com/123456789",
                "https://www.youtube.com/watch?v=tooShort",
                "https://www.youtube.com/watch?x=1#v=dQw4w9WgXcQ",
                "https://www.youtube.com/watch?xv=dQw4w9WgXcQ",
                "https://www.youtube.com/channel/UCdQw4w9WgXcQ",
                "not a url"
        })
        @DisplayName("rejects links without a YouTube video id")
        void rejectsNonYouTubeUrls(String videoUrl) {
            assertThatThrownBy(() -> mediaService.createYouTubeMedia(null, videoUrl, null, null, "en"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(mediaRepository, never()).save(any());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("alt text over 255 chars -> IllegalArgumentException instead of a failed insert")
        void rejectsTooLongAltText(boolean english) {
            String tooLong = "a".repeat(256);

            assertThatThrownBy(() -> mediaService.createYouTubeMedia(null, "https://youtu.be/dQw4w9WgXcQ",
                    english ? tooLong : null, english ? null : tooLong, "en"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(mediaRepository, never()).save(any());
        }

        @Test
        @DisplayName("video URL over 500 chars -> IllegalArgumentException instead of a failed insert")
        void rejectsTooLongVideoUrl() {
            String tooLong = "https://youtu.be/dQw4w9WgXcQ?x=" + "a".repeat(470);

            assertThatThrownBy(() -> mediaService.createYouTubeMedia(null, tooLong, null, null, "en"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(mediaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateMedia")
    class UpdateMedia {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("alt text over 255 chars -> IllegalArgumentException instead of a failed update")
        void rejectsTooLongAltText(boolean english) {
            UUID id = UUID.randomUUID();
            String tooLong = "a".repeat(256);

            assertThatThrownBy(() -> mediaService.updateMedia(id,
                    english ? tooLong : null, english ? null : tooLong, null, "en"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(mediaRepository, never()).save(any());
        }

        @Test
        @DisplayName("alt texts within the limit are saved")
        void updatesAltTexts() {
            UUID id = UUID.randomUUID();
            given(mediaRepository.findById(id)).willReturn(Optional.of(imageMedia(id)));
            given(mediaRepository.save(any(Media.class))).willAnswer(invocation -> invocation.getArgument(0));

            MediaDto result = mediaService.updateMedia(id, "a".repeat(255), "b", null, "en");

            assertThat(result.getAltTextEn()).hasSize(255);
            assertThat(result.getAltTextPl()).isEqualTo("b");
        }
    }

    @Nested
    @DisplayName("deleteMedia")
    class DeleteMedia {

        @BeforeEach
        void startTransactionSynchronization() {
            TransactionSynchronizationManager.initSynchronization();
        }

        @AfterEach
        void clearTransactionSynchronization() {
            TransactionSynchronizationManager.clearSynchronization();
        }

        private void commit() {
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        }

        @Test
        @DisplayName("YOUTUBE media: deletes the row and never touches storage")
        void deletesYouTubeRowWithoutStorage() throws Exception {
            UUID id = UUID.randomUUID();
            Media media = youTubeMedia(id);
            given(mediaRepository.findById(id)).willReturn(Optional.of(media));

            mediaService.deleteMedia(id);
            commit();

            verify(mediaRepository).delete(media);
            verify(storageService, never()).delete(any());
        }

        @Test
        @DisplayName("IMAGE media: deletes the row, then the file after commit")
        void deletesImageRowThenFileAfterCommit() throws Exception {
            UUID id = UUID.randomUUID();
            Media media = imageMedia(id);
            given(mediaRepository.findById(id)).willReturn(Optional.of(media));

            mediaService.deleteMedia(id);
            verify(storageService, never()).delete(any());
            commit();

            InOrder order = inOrder(mediaRepository, storageService);
            order.verify(mediaRepository).delete(media);
            order.verify(storageService).delete(media.getFilename());
        }

        @Test
        @DisplayName("rolled back delete keeps the file")
        void rollbackKeepsFile() throws Exception {
            UUID id = UUID.randomUUID();
            given(mediaRepository.findById(id)).willReturn(Optional.of(imageMedia(id)));

            mediaService.deleteMedia(id);
            // afterCommit is never called when the transaction rolls back

            verify(storageService, never()).delete(any());
        }

        @Test
        @DisplayName("file delete failure after commit is not propagated")
        void fileDeleteFailureIsNotPropagatedAfterCommit() throws Exception {
            UUID id = UUID.randomUUID();
            Media media = imageMedia(id);
            given(mediaRepository.findById(id)).willReturn(Optional.of(media));
            willThrow(new IOException("disk gone")).given(storageService).delete(media.getFilename());

            mediaService.deleteMedia(id);

            assertThatCode(this::commit).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("deleteAllForPost")
    class DeleteAllForPost {

        private final UUID postId = UUID.randomUUID();

        @BeforeEach
        void startTransactionSynchronization() {
            TransactionSynchronizationManager.initSynchronization();
        }

        @AfterEach
        void clearTransactionSynchronization() {
            TransactionSynchronizationManager.clearSynchronization();
        }

        private void commit() {
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        }

        @Test
        @DisplayName("deletes every row, then only the stored files after commit")
        void deletesRowsThenFilesAfterCommit() throws Exception {
            Media first = imageMedia(UUID.randomUUID());
            Media second = imageMedia(UUID.randomUUID());
            Media video = youTubeMedia(UUID.randomUUID());
            List<Media> media = List.of(first, second, video);
            given(mediaRepository.findByPostId(postId)).willReturn(media);

            mediaService.deleteAllForPost(postId);
            verify(storageService, never()).delete(any());
            commit();

            InOrder order = inOrder(mediaRepository, storageService);
            order.verify(mediaRepository).deleteAll(media);
            order.verify(storageService).delete(first.getFilename());
            order.verify(storageService).delete(second.getFilename());
            verify(storageService, times(2)).delete(any());
        }

        @Test
        @DisplayName("rolled back delete keeps the files")
        void rollbackKeepsFiles() throws Exception {
            given(mediaRepository.findByPostId(postId))
                    .willReturn(List.of(imageMedia(UUID.randomUUID()), imageMedia(UUID.randomUUID())));

            mediaService.deleteAllForPost(postId);
            // afterCommit is never called when the transaction rolls back

            verify(storageService, never()).delete(any());
        }

        @Test
        @DisplayName("a failed file delete is not propagated and does not stop the other files")
        void fileDeleteFailureDoesNotStopTheRest() throws Exception {
            Media broken = imageMedia(UUID.randomUUID());
            Media fine = imageMedia(UUID.randomUUID());
            given(mediaRepository.findByPostId(postId)).willReturn(List.of(broken, fine));
            willThrow(new IOException("disk gone")).given(storageService).delete(broken.getFilename());

            mediaService.deleteAllForPost(postId);

            assertThatCode(this::commit).doesNotThrowAnyException();
            verify(storageService).delete(fine.getFilename());
        }

        @Test
        @DisplayName("a post without media touches neither rows nor storage")
        void postWithoutMedia() throws Exception {
            given(mediaRepository.findByPostId(postId)).willReturn(List.of());

            mediaService.deleteAllForPost(postId);
            commit();

            verify(storageService, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("MediaDto.from")
    class MediaDtoMapping {

        @Test
        @DisplayName("should resolve alt text per locale and keep both raw alt texts")
        void shouldResolveAltTextPerLocale() {
            Media media = imageMedia(UUID.randomUUID());
            media.setAltTextEn("A rose");
            media.setAltTextPl("Róża");

            assertThat(MediaDto.from(media, "pl").getAltText()).isEqualTo("Róża");
            MediaDto en = MediaDto.from(media, "en");
            assertThat(en.getAltText()).isEqualTo("A rose");
            assertThat(en.getAltTextEn()).isEqualTo("A rose");
            assertThat(en.getAltTextPl()).isEqualTo("Róża");
            assertThat(en.getUrl()).isEqualTo(media.getUrl());
        }

        @Test
        @DisplayName("should map a media item without a post to a null postId")
        void shouldMapUnassignedMedia() {
            Media media = youTubeMedia(UUID.randomUUID());

            MediaDto dto = MediaDto.from(media, "en");

            assertThat(dto.getPostId()).isNull();
            assertThat(dto.getVideoUrl()).isEqualTo("https://youtu.be/dQw4w9WgXcQ");
        }

        @Test
        @DisplayName("should carry the id of the owning post")
        void shouldMapPostId() {
            UUID postId = UUID.randomUUID();
            Media media = imageMedia(UUID.randomUUID());
            media.setPost(Post.builder().id(postId).build());

            assertThat(MediaDto.from(media, "en").getPostId()).isEqualTo(postId);
        }
    }
}
