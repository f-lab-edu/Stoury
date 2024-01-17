package com.stoury.service;

import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    /**
     * reservedImagePaths.first: 저장해야할 파일
     * reservedImagePaths.second: 저장해야할 경로
     * @param reservedImagePaths
     */
    void saveFilesAtPath(Pair<MultipartFile, String> reservedImagePaths);
}
