package com.stoury.service;

import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Component
@Profile("dev")
public class FileStorageService implements StorageService {
    @Override
    public void saveFilesAtPath(Pair<MultipartFile, String> reservedImagePaths) {
        MultipartFile fileToSave = reservedImagePaths.getFirst();
        String fullPath = reservedImagePaths.getSecond();
        int lastIndexOfSlash = fullPath.lastIndexOf("/");

        String directory = fullPath.substring(0, lastIndexOfSlash);
        String fileName = fullPath.substring(lastIndexOfSlash + 1);

        Path path = Paths.get(directory, fileName);

        try {
            if (Files.exists(path)) {
                throw new FileAlreadyExistsException(fullPath);
            }
            Files.copy(fileToSave.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
