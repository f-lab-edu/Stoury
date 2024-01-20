package com.stoury.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static com.stoury.utils.SupportedFileType.*;

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
}
