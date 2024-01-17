package com.stoury.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.stoury.utils.FileUtils.FileType.*;

public class FileUtils {
    public static FileType getFileType(MultipartFile file){
        String contentType = file.getContentType();

        return switch (Objects.requireNonNull(contentType)) {
            case "image/jpeg" -> JPG;
            case "video/mp4" -> MP4;
            default -> OTHER;
        };
    }



    @AllArgsConstructor
    @Getter
    public enum FileType{
        JPG("/images", ".jpeg"), MP4("/videos", ".mp4"), OTHER("/other", "");
        private final String path;
        private final String extension;
    }
}
