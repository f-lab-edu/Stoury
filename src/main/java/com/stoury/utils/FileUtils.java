package com.stoury.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.StringJoiner;

public class FileUtils {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static String getFileNameByCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS"));
    }

    public static Path createFilePath(String fileName, String... directories) {
        StringJoiner joiner = new StringJoiner(FILE_SEPARATOR, FILE_SEPARATOR, "");

        Arrays.stream(directories).forEach(joiner::add);
        joiner.add(fileName);

        return Paths.get(joiner.toString());
    }
}
