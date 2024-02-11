package com.stoury.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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
                + UUID.randomUUID().toString().substring(0,8) + getFileNameByCurrentTime(file);
    }

    public static boolean isImage(String path) {
        return path.endsWith(SupportedFileType.JPG.getExtension());
    }
}
