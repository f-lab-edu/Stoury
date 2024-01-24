package com.stoury.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum SupportedFileType {
    JPG("images", ".jpeg", "image/jpeg"),
    MP4("videos", ".mp4", "video/mp4"),
    OTHER("other", "", "other/other");
    private final String type;
    private final String extension;
    private final String acceptContentType;

    public static SupportedFileType getFileType(MultipartFile file) {
        String contentType = file.getContentType();

        return Arrays.stream(SupportedFileType.values())
                .filter(fileType -> fileType.hasAcceptContentType(contentType))
                .findFirst()
                .orElse(OTHER);
    }

    private boolean hasAcceptContentType(String contentType) {
        return acceptContentType.equals(contentType);
    }

    public static boolean isUnsupportedFile(MultipartFile file) {
        return OTHER.equals(getFileType(file));
    }
}
