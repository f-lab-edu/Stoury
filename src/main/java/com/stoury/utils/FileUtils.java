package com.stoury.utils;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.StringJoiner;

public class FileUtils {
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static String getFileNameByCurrentTime(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
                + FILE_SEPARATOR + originalFilename;
    }

    public static String createFilePath(MultipartFile file, String pathPrefix) {
        SupportedFileType fileType = SupportedFileType.getFileType(file);
        return pathPrefix + FILE_SEPARATOR + fileType.getType() + FILE_SEPARATOR + getFileNameByCurrentTime(file);
    }
}
