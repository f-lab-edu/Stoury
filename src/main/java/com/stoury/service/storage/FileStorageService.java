package com.stoury.service.storage;

import com.stoury.event.GraphicSaveEvent;
import com.stoury.exception.graphiccontent.GraphicContentsException;
import com.stoury.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class FileStorageService implements StorageService {
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public void saveFileAtPath(MultipartFile fileToSave, Path path) {
        try {
            if (Files.exists(path)) {
                throw new FileAlreadyExistsException(path.toString());
            }
            FileUtils.createIfAbsentFile(path);
            Files.copy(fileToSave.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            eventPublisher.publishEvent(new GraphicSaveEvent(this, fileToSave, path.toString())); // NOSONAR
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
            FileUtils.deleteIfPresentFile(path);
        } catch (IOException e) {
            throw new GraphicContentsException("Error occur while deleting a file : " + path.getFileName(), e);
        }
    }
}
