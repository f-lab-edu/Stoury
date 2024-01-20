package com.stoury.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Component
@Profile("dev")
public class FileStorageService implements StorageService {

    @Override
    public void saveFilesAtPath(MultipartFile fileToSave, Path path) {
        try {
            if (Files.exists(path)) {
                throw new FileAlreadyExistsException(path.toString());
            }
            Files.copy(fileToSave.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
