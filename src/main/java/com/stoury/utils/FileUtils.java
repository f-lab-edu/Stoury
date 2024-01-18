package com.stoury.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static com.stoury.utils.FileUtils.SupportedFileType.*;

public class FileUtils {
    public static SupportedFileType getFileType(MultipartFile file) {
        String contentType = file.getContentType();


        return switch (Objects.requireNonNull(contentType)) {
            case "image/jpeg" -> JPG;
            case "video/mp4" -> MP4;
            default -> OTHER;
        };
    }

    public static String getFileNameByCurrentTime() {
        LocalDateTime nanoLocalDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        return nanoLocalDateTime.toString()
                .replaceAll("[-:T.]", "_");
    }

    @AllArgsConstructor
    @Getter
    public enum SupportedFileType {
        JPG("/images", ".jpeg"), MP4("/videos", ".mp4"), OTHER("/other", "");
        private final String path;
        private final String extension;
    }
}
