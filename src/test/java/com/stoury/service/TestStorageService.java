package com.stoury.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Component
@Profile("test")
public class TestStorageService implements StorageService {
    @Override
    public void saveFilesAtPath(MultipartFile fileToSave, Path path) {

    }
}
