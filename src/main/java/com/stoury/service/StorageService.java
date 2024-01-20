package com.stoury.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void saveFilesAtPath(MultipartFile fileToSave, String path);
}
