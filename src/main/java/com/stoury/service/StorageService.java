package com.stoury.service;

import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    void saveFilesAtPath(Pair<MultipartFile, String> reservedImagePaths);
}
