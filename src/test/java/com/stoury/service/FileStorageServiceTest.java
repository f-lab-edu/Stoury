package com.stoury.service;

import com.stoury.service.storage.FileStorageService;
import com.stoury.utils.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

class FileStorageServiceTest {
    FileStorageService fileStorageService = new FileStorageService();

    final String PATH_PREFIX = "src/test/resources/storagetest";
    final Path startPath = Paths.get(PATH_PREFIX);

    @BeforeEach
    void setup() {
        FileUtils.deleteRecursivelyDirectory(startPath);
        FileUtils.createIfAbsentDirectory(startPath);
    }

    @AfterEach
    void teardown() {
        FileUtils.deleteRecursivelyDirectory(startPath);
    }

    @Test
    @DisplayName("파일이 경로에 잘 써지는지 테스트")
    void testFileWriteSuccess() {
        MockMultipartFile file = new MockMultipartFile("Something", "file1", "image/jpeg", new byte[0]);
        Path savePath = Paths.get(PATH_PREFIX + "/file1.jpg");
        fileStorageService.saveFileAtPath(file, savePath);

        Assertions.assertThat(Files.exists(Path.of(PATH_PREFIX, "file1.jpg")))
                .isTrue();
    }
}
