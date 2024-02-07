package com.stoury.service.storage;

import com.stoury.exception.graphiccontent.GraphicContentsException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

@Component
@Profile("dev")
public class FileStorageService implements StorageService {
    @Override
    public void saveFileAtPath(MultipartFile fileToSave, Path path) {
        try {
            if (Files.exists(path)) {
                throw new FileAlreadyExistsException(path.toString());
            }
            Files.copy(fileToSave.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new GraphicContentsException("Error occur while saving a file : " + fileToSave.getOriginalFilename(), e);
        }
    }

    @Override
    public void deleteFileAtPath(Path path) {
        try {
            if (!Files.exists(path)) {
                throw new FileNotFoundException(path.toString());
            }
            Files.delete(path);
        } catch (IOException e) {
            throw new GraphicContentsException("Error occur while deleting a file : " + path.getFileName(), e);
        }
    }
}
