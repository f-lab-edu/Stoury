package com.stoury.service

import com.stoury.service.storage.FileStorageService
import com.stoury.utils.FileUtils
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileStorageServiceTest extends Specification {
    def eventPublisher = Mock(ApplicationEventPublisher)
    def fileStorageService = new FileStorageService(eventPublisher)
    final String PATH_PREFIX = "src/test/resources/storagetest";
    final Path startPath = Paths.get(PATH_PREFIX);


    def cleanup() {
        FileUtils.deleteRecursivelyDirectory(startPath);
    }

    def "지정경로에 파일 생성"() {
        given:
        MockMultipartFile file = new MockMultipartFile("Something", "file1", "image/jpeg", new byte[0]);
        def path = Paths.get(PATH_PREFIX + "/file1.jpeg")
        when:
        fileStorageService.saveFileAtPath(file, path)
        then:
        Files.exists(path)
    }
}
