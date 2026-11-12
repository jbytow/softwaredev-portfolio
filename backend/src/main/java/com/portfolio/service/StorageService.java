package com.portfolio.service;

import com.portfolio.entity.MediaType;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StorageService {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.allowed-types}")
    private String allowedTypes;

    private record StoredType(String extension, MediaType mediaType, String subDirectory) {
    }

    // Single source of MIME knowledge: what may be stored, under which extension and subdirectory,
    // and which MediaType it becomes. app.upload.allowed-types can only narrow this list.
    private static final Map<String, StoredType> STORED_TYPES = Map.of(
            "image/jpeg", new StoredType("jpg", MediaType.IMAGE, "images"),
            "image/png", new StoredType("png", MediaType.IMAGE, "images"),
            "image/gif", new StoredType("gif", MediaType.IMAGE, "images"),
            "image/webp", new StoredType("webp", MediaType.IMAGE, "images"),
            "video/mp4", new StoredType("mp4", MediaType.VIDEO, "videos"),
            "video/webm", new StoredType("webm", MediaType.VIDEO, "videos"),
            "application/pdf", new StoredType("pdf", MediaType.PDF, "documents")
    );

    // Older uploads kept the client's extension, so files named .jpeg or .jfif are still on disk
    private static final Map<String, String> LEGACY_EXTENSIONS = Map.of(
            "jpeg", "image/jpeg",
            "jfif", "image/jpeg"
    );

    private static final Map<String, String> CONTENT_TYPES_BY_EXTENSION = Stream.concat(
                    STORED_TYPES.entrySet().stream()
                            .map(entry -> Map.entry(entry.getValue().extension(), entry.getKey())),
                    LEGACY_EXTENSIONS.entrySet().stream())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Set<String> MEDIA_SUBDIRECTORIES = STORED_TYPES.values().stream()
            .map(StoredType::subDirectory)
            .collect(Collectors.toUnmodifiableSet());

    private Path rootLocation;
    private Set<String> allowedMimeTypes;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }

        allowedMimeTypes = Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .filter(type -> !type.isEmpty())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> unmapped = allowedMimeTypes.stream()
                .filter(type -> !STORED_TYPES.containsKey(type))
                .collect(Collectors.toSet());
        if (!unmapped.isEmpty()) {
            throw new IllegalStateException("app.upload.allowed-types contains types without a storage mapping: "
                    + unmapped + "; supported: " + STORED_TYPES.keySet());
        }
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }

        // The extension follows the validated MIME type; the client's name is not trusted
        StoredType storedType = STORED_TYPES.get(contentType);
        String filename = UUID.randomUUID() + "." + storedType.extension();

        String subDir = storedType.subDirectory();
        Path destinationDir = rootLocation.resolve(subDir);
        Files.createDirectories(destinationDir);

        Path destinationFile = destinationDir.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return subDir + "/" + filename;
    }

    public Resource loadAsResource(String filename) {
        Path file = resolveInsideRoot(filename);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            throw new EntityNotFoundException("Media file not found: " + filename);
        }
        return new FileSystemResource(file);
    }

    public void delete(String filename) throws IOException {
        Files.deleteIfExists(resolveInsideRoot(filename));
    }

    public MediaType getMediaType(String mimeType) {
        StoredType storedType = STORED_TYPES.get(mimeType);
        if (storedType == null) {
            throw new IllegalArgumentException("File type not allowed: " + mimeType);
        }
        return storedType.mediaType();
    }

    public static String contentTypeFor(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return CONTENT_TYPES_BY_EXTENSION.getOrDefault(extension, "application/octet-stream");
    }

    public static boolean isMediaSubdirectory(String subDir) {
        return MEDIA_SUBDIRECTORIES.contains(subDir);
    }

    // Names come from URLs and the DB; normalizing and checking the prefix keeps them inside the
    // upload root even if the HTTP firewall that rejects ".." is ever relaxed.
    private Path resolveInsideRoot(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        if (!file.startsWith(rootLocation) || file.equals(rootLocation)) {
            throw new EntityNotFoundException("Media file not found: " + filename);
        }
        return file;
    }
}
