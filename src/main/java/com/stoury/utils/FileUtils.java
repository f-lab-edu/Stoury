package com.stoury.utils;

import com.stoury.exception.graphiccontent.GraphicContentsException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static String getFileNameByCurrentTime(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
                + FILE_SEPARATOR + originalFilename;
    }

    public static String createFilePath(MultipartFile file, String pathPrefix) {
        SupportedFileType fileType = SupportedFileType.getFileType(file);
        return pathPrefix + FILE_SEPARATOR + fileType.getType() + FILE_SEPARATOR
                + UUID.randomUUID().toString().substring(0, 8) + getFileNameByCurrentTime(file);
    }

    public static boolean isImage(String path) {
        return path.endsWith(SupportedFileType.JPG.getExtension());
    }

    public static void createIfAbsentFile(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                throw new GraphicContentsException("NIO exception occur while creating file.", e);
            }
        }
    }

    public static void deleteIfPresentFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new GraphicContentsException("NIO exception occur while deleting file.", e);
        }
    }

    public static void deleteRecursivelyDirectory(Path path) {
        if (Files.exists(path)) {
            try (Stream<Path> nodes = Files.walk(path)) {
                nodes.forEach(node -> {
                    try {
                        if (!node.equals(path)) {
                            Files.delete(node);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("NIO exception occur, while traveling paths", e);
                    }
                });
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException("NIO exception occur while deleting directory.", e);
            }
        }
    }
}
