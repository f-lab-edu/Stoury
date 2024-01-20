package com.stoury.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {
    void saveFilesAtPath(MultipartFile fileToSave, Path path);
}
