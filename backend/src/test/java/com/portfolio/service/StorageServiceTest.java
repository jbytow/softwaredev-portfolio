package com.portfolio.service;

import com.portfolio.entity.MediaType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StorageService")
class StorageServiceTest {

    private static final String ALLOWED_TYPES =
            "image/jpeg,image/png,image/gif,image/webp,video/mp4,video/webm,application/pdf";
    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @TempDir
    Path tempDir;

    private Path root;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        root = tempDir.resolve("uploads");
        storageService = newStorageService(ALLOWED_TYPES);
    }

    private StorageService newStorageService(String allowedTypes) {
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "uploadPath", root.toString());
        ReflectionTestUtils.setField(service, "allowedTypes", allowedTypes);
        service.init();
        return service;
    }

    private static MockMultipartFile upload(String originalName, String contentType) {
        return new MockMultipartFile("file", originalName, contentType, new byte[]{1, 2, 3});
    }

    @Nested
    @DisplayName("init")
    class Init {

        @Test
        @DisplayName("allowlist entries are trimmed, blanks dropped and duplicates tolerated")
        void toleratesSpacesBlanksAndDuplicates() throws Exception {
            StorageService service = newStorageService(" image/png, image/png ,,");

            assertThat(service.store(upload("x.png", "image/png"))).startsWith("images/");
        }

        @Test
        @DisplayName("allowed type without a storage mapping stops startup")
        void unmappedTypeFailsStartup() {
            assertThatThrownBy(() -> newStorageService("image/png,image/avif"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("image/avif");
        }
    }

    @Nested
    @DisplayName("getMediaType")
    class GetMediaType {

        @ParameterizedTest
        @CsvSource({
                "image/jpeg, IMAGE", "image/png, IMAGE", "image/gif, IMAGE", "image/webp, IMAGE",
                "video/mp4, VIDEO", "video/webm, VIDEO", "application/pdf, PDF"
        })
        @DisplayName("maps every stored MIME type")
        void mapsStoredTypes(String mimeType, MediaType expected) {
            assertThat(storageService.getMediaType(mimeType)).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {"image/avif", "audio/mpeg", "text/html"})
        @DisplayName("unknown MIME type is rejected instead of becoming IMAGE")
        void rejectsUnknownType(String mimeType) {
            assertThatThrownBy(() -> storageService.getMediaType(mimeType))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("contentTypeFor")
    class ContentTypeFor {

        @ParameterizedTest
        @CsvSource({
                "a.jpg, image/jpeg", "a.jpeg, image/jpeg", "a.jfif, image/jpeg", "a.JPG, image/jpeg",
                "a.png, image/png", "a.gif, image/gif", "a.webp, image/webp",
                "a.mp4, video/mp4", "a.webm, video/webm", "a.pdf, application/pdf",
                "a.html, application/octet-stream", "a.svg, application/octet-stream",
                "noext, application/octet-stream", "trailingdot., application/octet-stream"
        })
        @DisplayName("served Content-Type follows the stored extension, unknown -> octet-stream")
        void mapsExtensions(String filename, String expected) {
            assertThat(StorageService.contentTypeFor(filename)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("isMediaSubdirectory")
    class IsMediaSubdirectory {

        @ParameterizedTest
        @CsvSource({"images, true", "videos, true", "documents, true", "other, false", "IMAGES, false", "wp-includes, false"})
        @DisplayName("only the subdirectories that store() writes to")
        void knowsStoredSubdirectories(String subDir, boolean expected) {
            assertThat(StorageService.isMediaSubdirectory(subDir)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("store")
    class Store {

        @Test
        @DisplayName("accepted type is stored under a UUID name in its subdirectory")
        void storesUnderUuidInSubdirectory() throws Exception {
            String stored = storageService.store(upload("photo.jpg", "image/jpeg"));

            assertThat(stored).matches("images/" + UUID_REGEX + "\\.jpg");
            assertThat(Files.readAllBytes(root.resolve(stored))).containsExactly(1, 2, 3);
        }

        @ParameterizedTest
        @CsvSource({
                "image/jpeg,      photo.jfif, images/,    .jpg",
                "image/jpeg,      noext,      images/,    .jpg",
                "image/jpeg,      photo.JPEG, images/,    .jpg",
                "image/png,       page.html,  images/,    .png",
                "image/gif,       anim.gif,   images/,    .gif",
                "image/webp,      pic.webp,   images/,    .webp",
                "video/mp4,       clip.mov,   videos/,    .mp4",
                "video/webm,      clip,       videos/,    .webm",
                "application/pdf, offer,      documents/, .pdf"
        })
        @DisplayName("subdirectory and extension come from the validated MIME type, not the client name")
        void extensionComesFromMimeType(String mimeType, String clientName, String subDir, String extension)
                throws Exception {
            String stored = storageService.store(upload(clientName, mimeType));

            assertThat(stored).matches(subDir + UUID_REGEX + "\\" + extension);
            assertThat(root.resolve(stored)).exists();
        }

        @ParameterizedTest
        @ValueSource(strings = {"text/html", "image/svg+xml", "application/octet-stream", "IMAGE/PNG", " image/png"})
        @DisplayName("content type outside the allowlist is rejected")
        void rejectsDisallowedType(String contentType) {
            assertThatThrownBy(() -> storageService.store(upload("x.png", contentType)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("mapped type left out of the configured allowlist is rejected")
        void rejectsMappedTypeOutsideConfiguredAllowlist() {
            StorageService pngOnly = newStorageService("image/png");

            assertThatThrownBy(() -> pngOnly.store(upload("photo.jpg", "image/jpeg")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("missing content type is rejected")
        void rejectsNullType() {
            assertThatThrownBy(() -> storageService.store(upload("x.jpg", null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("loadAsResource")
    class LoadAsResource {

        @Test
        @DisplayName("stored file is returned")
        void loadsStoredFile() throws Exception {
            String stored = storageService.store(upload("photo.jpg", "image/jpeg"));

            assertThat(storageService.loadAsResource(stored).getContentAsByteArray()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("missing file -> EntityNotFoundException")
        void missingFileIsNotFound() {
            assertThatThrownBy(() -> storageService.loadAsResource("images/missing.jpg"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("a directory is not served")
        void directoryIsNotFound() throws Exception {
            Files.createDirectories(root.resolve("images"));

            assertThatThrownBy(() -> storageService.loadAsResource("images"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"../outside.txt", "images/../../outside.txt", "../uploads-sibling/outside.txt"})
        @DisplayName("names escaping the upload root -> EntityNotFoundException even when the target exists")
        void pathOutsideRootIsNotFound(String name) throws Exception {
            Path outsideFile = root.resolve(name).normalize();
            Files.createDirectories(outsideFile.getParent());
            Files.write(outsideFile, new byte[]{9});

            assertThatThrownBy(() -> storageService.loadAsResource(name))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("absolute path -> EntityNotFoundException")
        void absolutePathIsNotFound() throws Exception {
            Path outsideFile = Files.write(tempDir.resolve("outside.txt"), new byte[]{9});

            assertThatThrownBy(() -> storageService.loadAsResource(outsideFile.toAbsolutePath().toString()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("stored file is removed")
        void deletesStoredFile() throws Exception {
            String stored = storageService.store(upload("photo.jpg", "image/jpeg"));

            storageService.delete(stored);

            assertThat(root.resolve(stored)).doesNotExist();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", ".", "images/.."})
        @DisplayName("names resolving to the upload root itself are rejected and the root survives")
        void rootItselfIsRejected(String name) {
            assertThatThrownBy(() -> storageService.delete(name))
                    .isInstanceOf(EntityNotFoundException.class);
            assertThat(root).isDirectory();
        }

        @ParameterizedTest
        @ValueSource(strings = {"../outside.txt", "images/../../outside.txt"})
        @DisplayName("names escaping the upload root are rejected and the target survives")
        void pathOutsideRootIsRejected(String name) throws Exception {
            Path outsideFile = root.resolve(name).normalize();
            Files.write(outsideFile, new byte[]{9});

            assertThatThrownBy(() -> storageService.delete(name))
                    .isInstanceOf(EntityNotFoundException.class);
            assertThat(outsideFile).exists();
        }
    }
}
