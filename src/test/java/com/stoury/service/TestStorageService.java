package com.stoury.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("test")
public class TestStorageService implements StorageService {
    @Override
    public void saveFilesAtPath(MultipartFile fileToSave, String path) {

    }
}
